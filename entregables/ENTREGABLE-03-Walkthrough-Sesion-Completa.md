# ENTREGABLE 3: WALKTHROUGH DE SESIÓN COMPLETA

## Descripción
Muestra paso a paso cómo **claude-agent-beta** abre el repositorio por primera vez, lee los archivos necesarios, elige una tarea, trabaja en ella, descubre conocimiento nuevo, toma una decisión técnica y cierra la sesión correctamente.

Este walkthrough es **realista y accionable**: un desarrollador IA puede copiarlo como protocolo exacto.

---

## FASE 0: PRE-FLIGHT GIT (antes de cualquier otra cosa)

**Comando ejecutado:**
```bash
$ git checkout main
$ git pull --rebase origin main
```

**Output esperado:**
```
Switched to branch 'main'
Current branch 'main' is up to date with 'origin/main'.
```

**Estado después:** 
- Agente tiene el código más reciente de main
- No hay conflictos ni archivos locales modificados

---

## FASE 1: INICIO DE SESIÓN (obligatorio, en orden exacto)

### Paso 1A: Leer `/ai/context.md` completo (15 segundos)

**Archivo leído:** `ai/context.md`

**Contenido (antes de ESTA sesión):**
```markdown
# PROJECT CONTEXT — NexaShop API
> Última actualización: 2025-03-07T14:30:00Z | Actualizado por: claude-agent-alpha

## ▸ ESTADO DEL SISTEMA
**Estado:** IN_DEVELOPMENT
**Motivo:** Middleware de autenticación en refactoring activo. No deployar a staging.

## ▸ TAREAS PRIORITARIAS AHORA
- [ ] TASK-047 — Implementar refresh token endpoint (crítica) — EN IN_PROGRESS
- [ ] TASK-048 — Migrar tests de auth (high) — BLOQUEADA por TASK-047
- [ ] TASK-051 — Cleanup código JWT antiguo (high) — PENDIENTE
- [ ] TASK-055 — Job de limpieza Redis (medium) — PENDIENTE

## ▸ AGENTES ACTIVOS
| Agente | Tarea | Desde | Archivos bloqueados |
|--------|-------|-------|-------------------|
| claude-agent-alpha | TASK-047 | 2025-03-07T07:00Z | src/auth/* |
```

**Decisión de agente-beta:** 
- ❌ No puedo tocar TASK-047 (bloqueado por claude-agent-alpha)
- ❌ No puedo trabajar TASK-048 (depende de TASK-047 que no está done)
- ✅ TASK-051 es buena candidata (high priority, no tiene dependencias)
- ❌ TASK-055 depende de TASK-046 que ya está done. Podría ser, pero TASK-051 es más urgente.

**Conclusión:** Enfocarse en **TASK-051 — Cleanup de código JWT antiguo**.

---

### Paso 1B: Leer `/ai/signals.yaml` (30 segundos)

**Archivo leído:** `ai/signals.yaml`

**Señales relevantes para claude-agent-beta:**
```yaml
- id: "SIG-006"
  type: "task_unblocked"
  from: "system"
  to: "any"
  task_id: "TASK-048"
  message: "TASK-047 completada. TASK-048 desbloqueada y lista para trabajar."
  created_at: "2025-03-07T14:00:00Z"
  read_by: []  # Aún no leída

- id: "SIG-007"
  type: "warning"
  from: "claude-agent-alpha"
  to: "any"
  message: "⚠️ TASK-047 en desarrollo. No mergear features a src/auth/ sin coordinar."
  created_at: "2025-03-07T07:20:00Z"
  read_by:
    - {agent: "claude-agent-beta", at: "2025-03-07T08:00:00Z"}  # Ya leída

- id: "SIG-008"
  type: "info"
  from: "human-pm"
  to: "any"
  message: "Reminder: Sprint 12 cierra el 2025-03-10. TASK-047, TASK-048, TASK-051 son critical."
  created_at: "2025-03-07T13:00:00Z"
  read_by: []
```

**Acción:** 
- Marcar SIG-006 y SIG-008 como read agregándome a read_by
- SIG-007 ya estaba leída (no tocar)

**Estado de signals.yaml después:**
```yaml
signals:
  # ... (SIG-001-005 no mostradas)
  - id: "SIG-006"
    read_by:
      - {agent: "claude-agent-beta", at: "2025-03-08T09:30:00Z"}  # ← AÑADIDO AHORA
  
  - id: "SIG-008"
    read_by:
      - {agent: "claude-agent-alpha", at: "2025-03-07T13:15:00Z"}
      - {agent: "claude-agent-beta", at: "2025-03-08T09:30:00Z"}  # ← AÑADIDO AHORA
```

---

### Paso 1C: Revisar `/ai/agent_lock.yaml` (15 segundos)

**Archivo leído:** `ai/agent_lock.yaml`

**Entrada activa:**
```yaml
active_agents:
  - agent_id: "claude-agent-alpha"
    session_id: "20250307-070000-claude-agent-alpha"
    last_heartbeat: "2025-03-07T14:25:00Z"  # ← Hace 1h05m (aún activo)
    current_task: "TASK-047"
    locked_files:
      - "src/auth/tokenService.js"
      - "src/auth/middleware.js"
      - "src/routes/auth.js"
      - "tests/auth/*"
```

**Verificación:**
- ✅ No tengo tareas en claimed/in_progress (límite = 1)
- ✅ Heartbeat de claude-agent-alpha es reciente (< 90 min) → agente sigue vivo
- ✅ Los archivos bloqueados (src/auth/*) no voy a tocar para TASK-051

---

### Paso 1D: Elige tarea en `/ai/tasks.yaml` (2 minutos)

**Archivo leído:** `ai/tasks.yaml`

**Criterios de filtrado:**
```
1. status = pending, reopened, o escalated ✓
2. assigned_agent = null ✓
3. depends_on = [] O todas las dependencias están done ✓
4. Tags coinciden con specialization_tags: testing, documentation, review ✓
5. Prioridad: critical > high > medium > low
```

**Candidatas encontradas:**

| TASK | Título | Status | Prioridad | Depends_on | Bloquea | Tags | Acción |
|------|--------|--------|-----------|-----------|---------|------|--------|
| TASK-048 | Migrar tests auth | blocked | high | [TASK-047] | none | testing | ❌ Aún bloqueada (TASK-047 aún en_progress) |
| TASK-051 | Cleanup JWT antiguo | pending | high | [] | none | backend,cleanup | ✅ LIBRE. Sin dependencias |
| TASK-052 | Documentar APIs Swagger | pending | medium | [TASK-047] | none | backend,docs | ❌ Bloqueada (TASK-047) |
| TASK-055 | Job limpieza Redis | pending | medium | [TASK-046] | none | backend,cache | ✅ LIBRE. TASK-046 ya done |

**Decisión:** Elegir entre TASK-051 (high, cleanup) vs TASK-055 (medium, caching).
- TASK-051 = high priority, más urgente para cierre de sprint
- TASK-055 = medium, pero necesario después

**Elegida:** **TASK-051** — Cleanup de código JWT antiguo

---

### Paso 1E: Reclamar la tarea en `/ai/tasks.yaml`

**Cambios realizados:**

ANTES:
```yaml
  - id: "TASK-051"
    title: "Cleanup: remover código JWT antiguo"
    status: "pending"
    assigned_agent: null
    claimed_at: null
```

DESPUÉS:
```yaml
  - id: "TASK-051"
    title: "Cleanup: remover código JWT antiguo"
    status: "claimed"  # ← CAMBIO
    assigned_agent: "claude-agent-beta"  # ← CAMBIO
    claimed_at: "2025-03-08T09:35:00Z"  # ← CAMBIO
    
    # Nuevo entry en state_history
    state_history:
      - {from: null, to: "pending", at: "2025-03-03T09:00:00Z", by: "human-pm"}
      - {from: "pending", to: "claimed", at: "2025-03-08T09:35:00Z", by: "claude-agent-beta"}  # ← NUEVO
```

**Git:**
```bash
git add ai/tasks.yaml
git commit -m "ai: claim TASK-051 cleanup JWT code [TASK-051]"
git push origin main
```

---

### Paso 1F: Registrarse en `/ai/agent_lock.yaml`

**Cambio realizado:**

```yaml
active_agents:
  - agent_id: "claude-agent-alpha"
    # ... entrada existente ...

  - agent_id: "claude-agent-beta"  # ← NUEVA ENTRADA
    session_id: "20250308-093500-claude-agent-beta"
    started_at: "2025-03-08T09:35:00Z"
    last_heartbeat: "2025-03-08T09:35:00Z"
    current_task: "TASK-051"
    status: "working"
    
    working_branch: "feat/TASK-051-cleanup-jwt"
    base_branch: "main"
    merge_strategy: "squash"
    
    locked_files:
      - "src/auth/tokenService.js"
      - "src/auth/oldJWT.js"
      - ".env.example"
      - "src/routes/auth.js"
      - "tests/auth/oldJWT.test.js"
    
    notes: "Removiendo JWT 24h estático. Encontrado endpoint legacy para mantener compat."
```

**Git:**
```bash
git add ai/agent_lock.yaml
git commit -m "ai: register claude-agent-beta for TASK-051 [TASK-051]"
git push origin main
```

---

### Paso 1G: Crear archivo de sesión

**Archivo creado:** `ai/sessions/20250308-093500-claude-agent-beta.md`

```markdown
# SESSION: 20250308-093500-claude-agent-beta

| Campo | Valor |
|-------|-------|
| **Agente** | claude-agent-beta |
| **Tarea principal** | TASK-051 |
| **Inicio** | 2025-03-08T09:35:00Z |
| **Fin** | EN PROGRESO |
| **Estado al cerrar** | EN PROGRESO |

## Objetivo de la sesión
Remover código JWT estático de 24h. El endpoint legacy debe devolver 403  Deprecated con documentación de migración hacia OAuth2.

## Contexto inicial
- OAuth2 (TASK-047) está en progreso
- Tests de auth (TASK-048) están bloqueados esperando TASK-047
- Esto es cleanup de código antiguo que ya no se usa
- Claude-agent-alpha tiene ownership de src/auth/
  NO voy a tocar middleware.js o tokenService.js desde ese archivo
  Voy a remover archivos viejos: src/auth/oldJWT.js, el enum, etc.
```

**Git:**
```bash
git add ai/sessions/20250308-093500-claude-agent-beta.md
git commit -m "ai: create session for TASK-051 [TASK-051]"
git push origin main
```

---

### Paso 1H: Cambiar status: claimed → in_progress

```yaml
  - id: "TASK-051"
    status: "in_progress"  # ← CAMBIO
    started_at: "2025-03-08T09:40:00Z"  # ← NUEVO
    
    state_history:
      - {from: "pending", to: "claimed", at: "2025-03-08T09:35:00Z", by: "claude-agent-beta"}
      - {from: "claimed", to: "in_progress", at: "2025-03-08T09:40:00Z", by: "claude-agent-beta"}  # ← NUEVO
```

---

## FASE 2: DURANTE EL TRABAJO

### Trabajo en TASK-051 (1 hora 45 minutos)

**Qué hace el agente:**

1. **Audit:** Busca dónde se usa JWT_SECRET_AUTH_24H
   ```bash
   grep -r "JWT_SECRET_AUTH_24H" src/ tests/ config/
   ```
   
   Resultado:
   - `src/auth/oldJWT.js` — archivo completo dedicado a JWT 24h
   - `src/routes/auth.js` línea 34 — endpoint POST /auth/legacy/login que usa oldJWT
   - `.env.example` — variable de entorno JWT_SECRET_AUTH_24H
   - `tests/auth/oldJWT.test.js` — suite de tests para JWT viejo

2. **Remover archivos:**
   ```bash
   rm src/auth/oldJWT.js
   rm tests/auth/oldJWT.test.js
   ```

3. **Actualizar src/routes/auth.js:**
   ```javascript
   // ANTES
   router.post('/auth/legacy/login', (req, res) => {
     const token = jwt.sign({ userId: req.body.userId }, process.env.JWT_SECRET_AUTH_24H, { expiresIn: '24h' });
     res.json({ token });
   });
   
   // DESPUÉS
   router.post('/auth/legacy/login', (req, res) => {
     res.status(403).json({
       error: 'Deprecated endpoint',
       message: 'JWT 24h authentication has been deprecated. Use POST /auth/login + refresh token flow.',
       documentation: 'https://api.nexashop.local/docs#oauth2-auth',
       migration_guide: 'See https://github.com/nexashop/api/wiki/OAuth2-Migration'
     });
   });
   ```

4. **Actualizar .env.example:**
   ```bash
   # REMOVER línea:
   # JWT_SECRET_AUTH_24H=your-secret-key-here-24h-jwt
   
   # COMENTARIO NUEVO:
   # DEPRECATED: JWT_SECRET_AUTH_24H no es usado. OAuth2 ahora requiere:
   # - JWT_SECRET (para access tokens)
   # - JWT_REFRESH_SECRET (para refresh tokens)
   ```

5. **Actualize package.json del archivo con git:**
   ```bash
   git rm src/auth/oldJWT.js
   git rm tests/auth/oldJWT.test.js
   git add src/routes/auth.js
   git add .env.example
   ```

6. **Descubrimiento:** Durante la auditoría, encuentra que hay variables de entorno sin documentar.
   → Añade esto a `/ai/memory/environment_setup.md`

7. **Cada 15 minutos:** Actualiza heartbeat en agent_lock.yaml
   ```yaml
   last_heartbeat: "2025-03-08T10:20:00Z"  # Actualizar
   ```

---

### Problema encontrado durante el trabajo

**Síntoma:** El test `tests/auth/oldJWT.test.js` tiene referencias en `tests/auth/middleware.test.js` que van a romper si los remevo sin actualizar.

**Causa raíz:** No checkeé dependencias de imports en otros tests.

**Solución:** Ver `tests/auth/middleware.test.js` línea 45 → remover import de oldJWT. Sus tests ya no aplican en nuevo esquema OAuth2.

**Acción:** 
```javascript
// ANTES
import { generateOldJWT } from '../src/auth/oldJWT';

describe('Auth Middleware', () => {
  it('should validate old JWT tokens', () => {
    // ... test que ya no aplica
  });
});

// DESPUÉS
// Remover import completamente. Old JWT tests pertenecían a oldJWT.test.js que fue eliminado
```

---

## FASE 3: CIERRE DE SESIÓN

### Paso 3A: Verificar definition_of_done

Definition of done de TASK-051:
```
- [x] Código JWT 24h removido de src/auth/
- [x] Variable JWT_SECRET_AUTH_24H removida de .env.example
- [x] Endpoint legacy devuelve 403 Deprecated
- [x] Git history limpio
```

✅ **Todo cumplido. Task está lista un DONE.**

---

### Paso 3B: Cambiar status → done

```yaml
  - id: "TASK-051"
    status: "done"  # ← CAMBIO FINAL
    updated_at: "2025-03-08T11:25:00Z"
    
    state_history:
      - {from: "claimed", to: "in_progress", at: "2025-03-08T09:40:00Z", by: "claude-agent-beta"}
      - {from: "in_progress", to: "done", at: "2025-03-08T11:25:00Z", by: "claude-agent-beta"}  # ← NUEVO
    
    actual_effort: "1h 50m"  # ← RELLENO
```

---

### Paso 3C: Emitir señales necesarias

Nuevo entry en `signals.yaml`:

```yaml
  - id: "SIG-009"
    type: "task_unblocked"
    from: "claude-agent-beta"
    to: "any"
    task_id: null  # No hay tareas que dependan de TASK-051
    message: "TASK-051 (cleanup JWT code) completada. Legacy endpoint ahora devuelve 403 Deprecated."
    created_at: "2025-03-08T11:26:00Z"
    read_by: []
```

(En este caso no hay tareas que desbloquear, pero la señal documenta el completamiento)

---

### Paso 3D: Escribir entrada en change_log.md

```markdown
## [2025-03-08 11:25Z] TASK-051 — claude-agent-beta

**Cambio:** Removido código JWT estático de 24h. Endpoint legacy documentado como Deprecated.

**Archivos modificados:**
  - `src/auth/oldJWT.js` — eliminado
  - `tests/auth/oldJWT.test.js` — eliminado
  - `src/routes/auth.js` — endpoint /auth/legacy/login devuelve 403 + documentación
  - `.env.example` — variable JWT_SECRET_AUTH_24H comentada
  - `tests/auth/middleware.test.js` — removido import de oldJWT

**Motivo:** TASK-051. Cleanup de código legacy post-migración a OAuth2 (DEC-012).
           OAuth2 es el autenticador nuevo, JWT 24h ya no se usa.

**Impacto:** Clientes usando endpoint /auth/legacy/login recibirán error 403 con instrucciones de migración.
            Esto fuerza la migración a OAuth2 sin downtime (error manejable en cliente).

**Reversión:** `git revert {commit-sha}`

**Decisión relacionada:** DEC-012 (OAuth2 architecture)
```

---

### Paso 3E: Actualizar context.md si cambió algo

En este caso, el contexto del proyecto no cambió materialmente. TASK-051 era cleanup, no genera trabajo futuro. **No requiere actualizar context.md.**

---

### Paso 3F: Cerrar archivo de sesión

```markdown
## Estado al cerrar

**Resultado:** COMPLETADO
**Duración real:** 1h 50m
**Próximo agente debe:** N/A — tarea completamente resuelta

**Advertencias:** Clientes del endpoint /auth/legacy/login necesitan actualizar.
                  Error 403 incluye enlace a documentación de migración.

---

## Checkpoint para auditoría
- [x] Change_log.md actualizado
- [x] Signals.yaml con notificación de completamiento
- [x] Context.md no requería actualización
- [x] Sin secretos en la sesión
- [ ] Agent_lock.yaml será limpiado en siguiente paso
```

---

### Paso 3G: Eliminarse de agent_lock.yaml

**Borrar la entrada de claude-agent-beta:**

ANTES:
```yaml
active_agents:
  - agent_id: "claude-agent-alpha"
    # ... entrada ...
  
  - agent_id: "claude-agent-beta"  # ← REMOVER ESTA ENTRADA COMPLETAMENTE
    session_id: "20250308-093500-claude-agent-beta"
    # ...
```

DESPUÉS:
```yaml
active_agents:
  - agent_id: "claude-agent-alpha"
    # ... entrada ...
```

**Git:**
```bash
git add ai/agent_lock.yaml
git commit -m "ai: removed claude-agent-beta from active agents [TASK-051]"
git push origin main
```

---

## RESUMEN DE CAMBIOS

**Archivos modificados en total:**
- `ai/tasks.yaml` — claimed → in_progress → done
- `ai/agent_lock.yaml` — registred → actualizaciond heartbeat cada 15 min → eliminada
- `ai/signals.yaml` — un nuevo signal tipos task_unblocked
- `ai/change_log.md` — una entrada de cambio
- `ai/sessions/20250308-093500-claude-agent-beta.md` — archivo de sesión creado
- `src/auth/oldJWT.js` — eliminado
- `src/routes/auth.js` — endpoint legacy actualizado
- `.env.example` — variable removida
- `tests/auth/oldJWT.test.js` — eliminado
- `tests/auth/middleware.test.js` — import removido

**Commits realizados:**
1. `ai: claim TASK-051 cleanup JWT code`
2. `ai: register claude-agent-beta for TASK-051`
3. `ai: create session for TASK-051`
4. `feat: remove old JWT 24h code, add 403 Deprecated endpoint` (feature branch, luego squash)
5. `ai: removed claude-agent-beta from active agents` (cambio en agent_lock.yaml)
6. `ai: TASK-051 done, update tasks and changelog`

**Tiempo total:** ~2 horas (30 min planificación y lecturas, 90 min coding, 20 min cierre)

---

Este walkthrough demuestra que:
- ✅ Un agente puede completar una tarea completa en una sesión
- ✅ El protocolo es accionable y no demasiado pesado
- ✅ Los archivos de seguimiento se actualizan automáticamente
- ✅ El workflow de git es simple (commits directos a main para /ai/)
- ✅ Otros agentes pueden ver el progreso en tiempo real via signals.yaml
