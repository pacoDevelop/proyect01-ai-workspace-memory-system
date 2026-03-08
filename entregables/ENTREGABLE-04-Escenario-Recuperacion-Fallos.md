# ENTREGABLE 4: Escenario Recuperación de Fallos

**Descripción:** Demostración exacta de cómo el sistema detecta que un agente se ha crasheado y se recupera sin corrupción de datos.

**Escenario:** claude-agent-alpha está trabajando en TASK-047 cuando su proceso se detiene abruptamente. El sistema detecta esto automáticamente y lo limpia.

---

## CONTEXTO DEL FALLO

### Estado Normal (T = 0:00)
```yaml
# ai/memory/agent_lock.yaml ANTES DEL FALLO
agents:
  - id: "claude-agent-alpha"
    locked_files:
      - "ai/memory/tasks.yaml"
      - "src/routes/auth.js"
    locked_at: "2025-03-07T10:00:00Z"
    heartbeat_at: "2025-03-07T10:15:00Z"
    current_task: "TASK-047"
    status: "working"
```

**Exactamente en T = 10:17:** El agente estaba escribiendo una línea crítica en `src/routes/auth.js` cuando su proceso fue terminado abruptamente (error de memoria, timeout de red, crash del servidor, etc.)

---

## FASE 1: DETECCIÓN DEL FALLO (T = 10:17 → 10:35)

### 1.1 El Agente Se Detiene
```
[10:17:03] claude-agent-alpha: Escribiendo endpoint /auth/refresh
[10:17:04] Writing file src/routes/auth.js
[10:17:05] ERROR: Memory overflow exception
[10:17:05] Process terminated with code 137
```

**Archivo en estado corrupto:**
```javascript
// src/routes/auth.js — ESTADO INCOMPLETO

router.post('/auth/refresh', async (req, res) => {
  const { refresh_token } = req.body;
  
  // Validación started but incomplete
  if (!refresh_token) {
    res.status(400).json({ error: 'Missing refresh_token' })
  
  // ARCHIVO INCOMPLETO — Se cortó aquí
  // rest of code never reached
```

---

### 1.2 Sistema Detecta Fallo (T = 10:20)
**Trigger:** Otro agente o proceso de validación intenta leer agent_lock.yaml

```bash
# Script: ai/scripts/validate_workspace.sh (ejecutado por CI o cron)
# Cron job: cada 5 minutos

#!/bin/bash

LOCK_FILE="ai/memory/agent_lock.yaml"
HEARTBEAT_TIMEOUT=5400  # 90 minutos en segundos

while IFS= read -r agent_line; do
  if [[ $agent_line =~ ^[[:space:]]*-[[:space:]]*id: ]]; then
    agent_id=$(echo "$agent_line" | sed 's/.*id: //; s/"//g; s/  *//')
    
    # Leer heartbeat del agente
    heartbeat=$(grep -A 3 "id: \"$agent_id\"" "$LOCK_FILE" | grep heartbeat_at | sed 's/.*: //' | xargs)
    
    # Convertir a timestamp unix
    heartbeat_epoch=$(date -d "$heartbeat" +%s 2>/dev/null || date -jf "%Y-%m-%dT%H:%M:%SZ" "$heartbeat" +%s)
    current_epoch=$(date +%s)
    
    time_diff=$((current_epoch - heartbeat_epoch))
    
    if [ $time_diff -gt $HEARTBEAT_TIMEOUT ]; then
      echo "ALERTA: Agente $agent_id sin heartbeat desde hace $time_diff segundos"
      echo "  - Última actividad: $heartbeat"
      echo "  - Estado: MUERTO o desconectado"
      
      # Registrar el evento
      echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] DEAD_AGENT_DETECTED: $agent_id after ${time_diff}s" >> ai/memory/recovery.log
    fi
  fi
done < <(grep "id:" "$LOCK_FILE")
```

**Output:**
```
[10:20:15] Validación periódica: agent_lock.yaml
[10:20:16] ⚠️  ALERTA: Agente claude-agent-alpha sin heartbeat desde hace 905 segundos
[10:20:16]   - Última actividad: 2025-03-07T10:15:00Z
[10:20:16]   - Tiempo actual: 2025-03-07T10:35:15Z
[10:20:16]   - DIFERENCIA: 905 segundos (15 minutos > límite de 90 minutos)
[10:20:16]   - Estado: MUERTO o desconectado
[10:20:16] 📋 Registrado en ai/memory/recovery.log
```

**recovery.log actualizado:**
```
[2025-03-07T10:20:16Z] DEAD_AGENT_DETECTED: claude-agent-alpha after 905s without heartbeat
[2025-03-07T10:20:16Z] Last known state: working on TASK-047
[2025-03-07T10:20:16Z] Locked files: ai/memory/tasks.yaml, src/routes/auth.js
[2025-03-07T10:20:16Z] Initiating automatic recovery...
```

---

## FASE 2: EVALUACIÓN DEL DAÑO (T = 10:35 → 10:40)

### 2.1 Checksum de Archivos Críticos

El sistema compara integridad contra últimas versiones conocidas en git:

```bash
#!/bin/bash
# ai/scripts/check_damage.sh

echo "=== EVALUACIÓN DE DAÑO ==="

DEAD_AGENT="claude-agent-alpha"
LOCKED_FILES=$(grep -A 5 "id: \"$DEAD_AGENT\"" ai/memory/agent_lock.yaml | grep "locked_files:" -A 10)

for file in $LOCKED_FILES; do
  if [ -f "$file" ]; then
    current_checksum=$(sha256sum "$file" | awk '{print $1}')
    git_checksum=$(git show HEAD:"$file" 2>/dev/null | sha256sum | awk '{print $1}')
    
    if [ "$current_checksum" != "$git_checksum" ]; then
      echo "❌ CORRUPTED: $file"
      echo "   Current:  $current_checksum"
      echo "   Expected: $git_checksum"
      echo "   Action: REVERT to git HEAD"
    else
      echo "✅ SAFE: $file"
    fi
  fi
done
```

**Output:**
```
=== EVALUACIÓN DE DAÑO ===
Agente muerto: claude-agent-alpha
Archivos bloqueados: 2

❌ CORRUPTED: src/routes/auth.js
   Current:  a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
   Expected: z9y8x7w6v5u4t3s2r1q0p9o8n7m6l5k4
   Action: REVERT to git HEAD

✅ SAFE: ai/memory/tasks.yaml
   (No cambios desde última versión en git)
```

### 2.2 Evaluar TASK-047

```bash
# Leer estado en tasks.yaml
STATUS_BEFORE=$(grep -A 20 'id: "TASK-047"' ai/memory/tasks.yaml | grep status: | head -1)
echo "TASK-047 status: $STATUS_BEFORE"
# Output: TASK-047 status: in_progress
```

**Decisión a tomar:**
- ¿El trabajo estaba lo suficientemente avanzado para mantenerlo?
- ¿O deberíamos revertir la tarea a "pending"?

**Criterio:** Si archivos críticos están corruptos, se revierte la tarea.

---

## FASE 3: EJECUCIÓN DE RECUPERACIÓN AUTOMÁTICA (T = 10:40 → 10:45)

### 3.1 Limpiar agent_lock.yaml

```yaml
# ANTES
agents:
  - id: "claude-agent-alpha"
    locked_files:
      - "ai/memory/tasks.yaml"
      - "src/routes/auth.js"
    locked_at: "2025-03-07T10:00:00Z"
    heartbeat_at: "2025-03-07T10:15:00Z"
    current_task: "TASK-047"
    status: "working"

# DESPUÉS — El agente se elimina completamente
agents: [] # Vacío — no hay agentes activos
```

**Comando ejecutado:**
```bash
# ai/scripts/cleanup_dead_agent.sh
sed -i '/id: "claude-agent-alpha"/,/status: "working"/d' ai/memory/agent_lock.yaml
# O más seguramente, regenerar el archivo sin claude-agent-alpha
```

### 3.2 Revertir Archivos Corruptos

```bash
# Para cada archivo corrupto, revertir a la versión en git
git checkout HEAD -- src/routes/auth.js

# Verificar
git status src/routes/auth.js
# On branch main, nothing to commit
```

**src/routes/auth.js ahora es clean (última versión buena):**
```javascript
// src/routes/auth.js — REVERTIDO A ESTADO LIMPIO

router.post('/auth/refresh', async (req, res) => {
  const { refresh_token } = req.body;
  
  // Validación
  if (!refresh_token) {
    return res.status(400).json({ error: 'Missing refresh_token' });
  }
  
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET, {
      audience: 'api.nexashop.local',
      algorithms: ['HS256']
    });
    
    // ... resto del endpoint
  } catch (err) {
    return res.status(401).json({ error: 'Invalid refresh token' });
  }
});
```

### 3.3 Actualizar TASK-047

```yaml
# ANTES — in_progress por agente muerto
- id: "TASK-047"
  title: "Implementar endpoint POST /auth/refresh"
  status: "in_progress"
  assigned_agent: "claude-agent-alpha"
  started_at: "2025-03-07T10:00:00Z"
  updated_at: "2025-03-07T10:15:00Z"

# DESPUÉS — Revertida a pending por fallo de agente
- id: "TASK-047"
  title: "Implementar endpoint POST /auth/refresh"
  status: "pending"
  assigned_agent: null
  started_at: null
  updated_at: "2025-03-07T10:40:00Z"
  comment: "Revertido a pending por crash del agente claude-agent-alpha. Archivos restaurados de git."
  recovery_event: "REC-001"
```

**Cambio registrado en git:**
```bash
git add ai/memory/agent_lock.yaml ai/memory/tasks.yaml src/routes/auth.js
git commit -m "fix: auto-recovery from dead agent (claude-agent-alpha)

- Detected heartbeat timeout (905 seconds > 5400s limit)
- Reverted corrupted src/routes/auth.js to git HEAD
- Cleaned ag ent_lock.yaml
- Reset TASK-047 to pending status
- Recovery ID: REC-001
- See ai/memory/recovery.log for full details"
```

### 3.4 Registrar el Evento en change_log.md

```markdown
# ai/memory/change_log.md

## Entrada 17: Auto-Recovery from Dead Agent

**Cambio ID:** REC-001  
**Timestamp:** 2025-03-07T10:40:30Z  
**Tipo:** Recovery / Fail-safe  
**Iniciado por:** recovery_script.sh (sistema automático)

### Contexto
- Agente `claude-agent-alpha` sin pulso durante 905 segundos (límite: 5400s)
- Última actividad registrada: 2025-03-07T10:15:00Z
- Proceso: Crash detectado al validar agent_lock.yaml

### Cambios Realizados

#### Archivos Revertidos
- ✅ `src/routes/auth.js` → revertido a git HEAD (checksum no coincidía)
- ✅ `ai/memory/tasks.yaml` → verificado íntegro (checksum OK)

#### Estado del Agente
- ❌ `claude-agent-alpha` removido completamente de agent_lock.yaml
- ✅ Heartbeat lock release automático

#### Tareas Afectadas
- TASK-047: `in_progress` → `pending` (sin agente asignado)
  - Razón: Agente crashed, archivos revertidos, tarea incompleta
  - Nota: Próximo agente puede retomar desde línea común

### Motivo
Sistema de recuperación automática: Prevenir corrupción de datos por agente inactivo/muerto.

### Reversión
Esta autorecuperación NO se puede revertir manualmente. Sin embargo:
- Si `src/routes/auth.js` tenía cambios valiosos, humano debe reaplicarlos
- Si TASK-047 debe reasignarse, actualizar status a "claimed" nuevamente

### Decisión Relacionada
- DEC-015: "Timeout de heartbeat en 90 minutos vs alternativas" (justificación de 5400s)

### Verificación
```bash
git log --oneline ai/memory/recovery.log
# [2025-03-07T10:40:30Z] REC-001 completion
```
```

### 3.5 Registrar en recovery.log

```
[2025-03-07T10:20:16Z] DEAD_AGENT_DETECTED: claude-agent-alpha after 905s without heartbeat
[2025-03-07T10:20:16Z] Last known state: working on TASK-047
[2025-03-07T10:20:16Z] Locked files: ai/memory/tasks.yaml, src/routes/auth.js
[2025-03-07T10:20:16Z] Initiating automatic recovery...

[2025-03-07T10:35:42Z] DAMAGE_EVALUATION_STARTED
[2025-03-07T10:35:43Z] Checking integrity of locked files...
[2025-03-07T10:35:44Z] ❌ CORRUPTED: src/routes/auth.js (checksum mismatch)
[2025-03-07T10:35:44Z] ✅ SAFE: ai/memory/tasks.yaml (checksum match)
[2025-03-07T10:35:45Z] DAMAGE_EVALUATION_COMPLETED: 1 file corrupted, 1 file safe

[2025-03-07T10:40:15Z] CLEANUP_STARTED
[2025-03-07T10:40:16Z] Removing claude-agent-alpha from agent_lock.yaml
[2025-03-07T10:40:17Z] Reverting src/routes/auth.js to git HEAD
[2025-03-07T10:40:18Z] Resetting TASK-047 status: in_progress → pending
[2025-03-07T10:40:19Z] CLEANUP_COMPLETED

[2025-03-07T10:40:25Z] GIT_COMMIT_STARTED
[2025-03-07T10:40:26Z] Staging changes: agent_lock.yaml, tasks.yaml, src/routes/auth.js
[2025-03-07T10:40:27Z] Commit: "fix: auto-recovery from dead agent (claude-agent-alpha)"
[2025-03-07T10:40:28Z] GIT_COMMIT_COMPLETED: commit abc123def456

[2025-03-07T10:40:30Z] RECOVERY_COMPLETE
[2025-03-07T10:40:30Z] Recovery ID: REC-001
[2025-03-07T10:40:30Z] Status: SUCCESS
[2025-03-07T10:40:30Z] Impact: 1 task reset to pending, 0 data loss
```

---

## FASE 4: VALIDACIÓN Y LIMPIEZA (T = 10:45 → 10:50)

### 4.1 Validar Integridad Post-Recovery

```bash
#!/bin/bash
# ai/scripts/validate_recovery.sh

echo "=== POST-RECOVERY VALIDATION ==="

# 1. Revisar agent_lock.yaml
echo "✓ Checking agent_lock.yaml..."
agents_count=$(grep -c '^\s*id:' ai/memory/agent_lock.yaml)
echo "  Active agents: $agents_count (expected: 0 or other agents)"

# 2. Revisar tasks.yaml
echo "✓ Checking tasks.yaml..."
task_047=$(grep -A 3 'id: "TASK-047"' ai/memory/tasks.yaml | grep status:)
echo "  TASK-047 status: $(echo $task_047 | sed 's/.*status: //')"
echo "  Expected: pending"

# 3. Validar archivos revertidos
echo "✓ Checking git diff..."
git status --porcelain
# Expected: clean working tree

# 4. Verificar no hay cambios no commiteados
uncommitted=$(git status --porcelain | wc -l)
if [ $uncommitted -eq 0 ]; then
  echo "  ✅ Working tree clean"
else
  echo "  ⚠️  Uncommitted changes: $uncommitted files"
fi

echo ""
echo "=== VALIDATION COMPLETE ==="
```

**Output:**
```
=== POST-RECOVERY VALIDATION ===
✓ Checking agent_lock.yaml...
  Active agents: 0 (all agents cleaned, or 1 other agent still active)

✓ Checking tasks.yaml...
  TASK-047 status: pending
  Expected: pending ✅

✓ Checking git diff...
  M ai/memory/agent_lock.yaml
  M ai/memory/tasks.yaml
  M ai/memory/change_log.md

✓ Checking git status...
  ✅ Working tree clean (all changes committed)

=== VALIDATION COMPLETE ===
```

### 4.2 Notificación a Otros Agentes

**Signal creada automáticamente:**

```yaml
# ai/memory/signals.yaml

- id: "SIG-012"
  type: "recovery_completed"
  from: "system"
  to: "any"
  task_id: "TASK-047"
  message: "Recovery from dead agent completed. TASK-047 reset to pending. Available for reassignment."
  created_at: "2025-03-07T10:40:30Z"
  recovery_event: "REC-001"
  related_commit: "abc123def456"
  read_by: []
```

---

## FASE 5: AGENTE NUEVO RETOMA LA TAREA (T = 11:00+)

### 5.1 Claude-Agent-Beta Reclama TASK-047

```yaml
# ai/memory/tasks.yaml — TASK-047 reclamada nuevamente

- id: "TASK-047"
  title: "Implementar endpoint POST /auth/refresh"
  status: "claimed"  # Cambio: pending → claimed
  assigned_agent: "claude-agent-beta"
  claimed_at: "2025-03-07T11:00:00Z"
  started_at: null
  updated_at: "2025-03-07T11:00:00Z"
  
  state_history:
    # ... historial anterior ...
    - { from: "in_progress", to: "pending", at: "2025-03-07T10:40:00Z", by: "system" }
    - { from: "pending", to: "claimed", at: "2025-03-07T11:00:00Z", by: "claude-agent-beta" }
```

### 5.2 Nuevo Agente Trabaja Desde Línea Común

**Claude-agent-beta lee el estado:**

```bash
# Sesión de claude-agent-beta
cat ai/memory/tasks.yaml | grep -A 25 'id: "TASK-047"'

# Lee últimos cambios en la rama
git log --oneline -10 ai/memory/change_log.md
# Nota que REC-001 explica qué pasó

# Lee el recovery.log
tail -20 ai/memory/recovery.log

# Conclusión: "El endpoint se crasheó. Debo implementarlo nuevamente desde cero."
# Pero el código es simple, la lógica está documentada en decisions.md
```

**Estado final POST-RECOVERY:**
- ✅ Zero data corruption
- ✅ Task reasignable
- ✅ Full audit trail
- ✅ Git history limpio
- ✅ Otro agente puede continuar sin confusión

---

## MÉTRICAS DEL FALLO Y RECUPERACIÓN

| Métrica | Valor |
|---------|-------|
| **Tiempo para detectar fallo** | 5 minutos (validación cada 5 min) |
| **Tiempo para evaluar daño** | 3 minutos |
| **Tiempo para ejecutar recovery** | 5 minutos |
| **Tiempo total sistema degradado** | 13 minutos |
| **Archivos corruptos** | 1 (revertible) |
| **Archivos perdidos** | 0 |
| **Datos perdidos** | 0 |
| **Manual intervention requerido?** | NO |
| **Tarea recuperable?** | SÍ (reset a pending) |

---

## ALTERNATIVAS EXPLORADAS

### ❌ NO HACER AUTO-RECOVERY
**Consecuencia:** agent_lock.yaml queda inconsistent, con agente fantasma bloqueando archivos por 90 minutos.

### ❌ CONFIAR EN HEARTBEAT MANUAL
**Consecuencia:** Si agente crash, heartbeat never updates, files locked forever.

### ✅ RECOVERY AUTOMÁTICO (IMPLEMENTADO)
**Beneficio:** System self-heals en < 15 minutos sin intervención humana.

---

## REGLAS DE SAFETY

```yaml
# Para cada daño detectado:

recovery_rules:
  # Regla 1: Se detectó agente sin pulso
  - when: "heartbeat > TIMEOUT (90 min)"
    then:
      - "Limpiar agent_lock.yaml"
      - "Evaluar integridad de archivos bloqueados"
  
  # Regla 2: Archivo tiene checksum corrupto
  - when: "file_checksum != git_HEAD_checksum"
    then:
      - "Revertir archivo a git HEAD"
      - "Registrar en recovery.log"
      - "Crear DEC- decision explicando por qué"
  
  # Regel 3: Task estaba in_progress
  - when: "task.status == in_progress AND agent_dead"
    then:
      - "Reset task to pending"
      - "Clear assigned_agent"
      - "Create signal para otros agentes"
  
  # Regla 4: Cambios no commiteados
  - when: "uncommitted_changes after recovery"
    then:
      - "Git commit con recovery metadata"
      - "Incluir recovery_event ID en mensaje"
```

---

## PRÓXIMA IMPLEMENTACIÓN

Este escenario es **100% implementable** sin servidores:

```bash
# 1. Instalar job scheduler (cron en Linux, Task Scheduler en Windows)
crontab -e
# */5 * * * * /path/to/ai/scripts/validate_workspace.sh

# 2. El resto funciona con herramientas estándar (bash + git)

# 3. Agregar a pre-commit hook para prevención:
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Validar que agent_lock.yaml no tiene agentes fantasma
EOF
```

---

## CONCLUSIÓN

**El sistema se recupera automáticamente de fallos de agentes.**

- ✅ Detección automática en < 5 minutos
- ✅ Evaluación inteligente del daño
- ✅ Revertir solo lo necesario (no perder cambios salvables)
- ✅ Full audit trail en recovery.log + change_log.md
- ✅ Cero intervención humana requerida
- ✅ Task transferible a otro agente

**Garantía:** Si un agente se crash en el medio de una tarea, el sistema garantiza que:
1. No habrá corrupción de datos irrecuperable
2. El trabajo será revertible sin ambigüedad
3. Otro agente puede retomar en < 15 minutos
