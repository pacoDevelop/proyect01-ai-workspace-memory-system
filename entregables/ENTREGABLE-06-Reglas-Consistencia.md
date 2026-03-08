# ENTREGABLE 6: Reglas de Consistencia para Cada Archivo

**Descripción:** Matriz de validación que define el estado "saludable", "degradado" y "corrupto" para cada archivo crítico del sistema.

---

## PRINCIPIO FUNDAMENTAL

Cada archivo en `/ai/memory/` sigue un schema definido. El sistema puede detectar automáticamente si está:
- ✅ **HEALTHY:** Cumple con schema + datos coherentes
- ⚠️ **DEGRADED:** Cumple schema pero datos incompletos o inconsistentes  
- ❌ **CORRUPTED:** Viola schema o contiene datos contradictorios

---

## ARCHIVO 1: `agent_lock.yaml`

### 1.1 Schema Esperado

```yaml
schema_version: "{version}"
min_compatible_version: "{min_version}"
last_updated: "{ISO8601_timestamp}"
last_updated_by: "{human_name_or_system}"

agents:
  - id: "{agent-unique-id}"
    locked_files:
      - "{file_path_1}"
      - "{file_path_2}"
    locked_at: "{ISO8601_timestamp}"
    heartbeat_at: "{ISO8601_timestamp}"
    current_task: "{TASK-id}"
    status: "working|blocked|paused"
```

### 1.2 Matriz de Estados

| Estado | Condición | Acción | Ejemplo |
|--------|-----------|--------|---------|
| ✅ HEALTHY | `agents: []` (vacío) O agente con `heartbeat_at < 90 min` | NADA | Sin agentes activos, o todos tienen pulse reciente |
| ⚠️ DEGRADED | Agente presente pero `heartbeat_at` > 30 min | MONITOR | Agente dejó de actualizar hace 30-90 min |
| ❌ CORRUPTED | Agente con `heartbeat_at` > 90 min | CLEANUP | Agente muerto, remover automáticamente |

### 1.3 Script de Validación

```bash
#!/bin/bash
# ai/scripts/validate_agent_lock.sh

FILE="ai/memory/agent_lock.yaml"
TIMEOUT_HARD=5400  # 90 minutos en segundos
TIMEOUT_SOFT=1800  # 30 minutos en segundos

check_agent_lock() {
  echo "Validating: $FILE"
  
  # Paso 1: Verificar que el archivo existe
  if [ ! -f "$FILE" ]; then
    echo "❌ MISSING: $FILE"
    return 1
  fi
  
  # Paso 2: Verificar YAML syntax
  if ! python3 -c "import yaml; yaml.safe_load(open('$FILE'))" 2>/dev/null; then
    echo "❌ CORRUPTED: Invalid YAML syntax"
    return 1
  fi
  
  # Paso 3: Verificar schema_version existe
  if ! grep -q "schema_version:" "$FILE"; then
    echo "❌ CORRUPTED: Missing schema_version"
    return 1
  fi
  
  # Paso 4: Verificar cada agente
  agents_count=$(grep -c "^\s*- id:" "$FILE" || echo "0")
  echo "Agents in lock: $agents_count"
  
  if [ $agents_count -eq 0 ]; then
    echo "✅ HEALTHY: No agents locked (clean state)"
    return 0
  fi
  
  # Paso 5: Validar heartbeats
  python3 << 'EOF'
import yaml
from datetime import datetime, timezone, timedelta

with open('ai/memory/agent_lock.yaml') as f:
  data = yaml.safe_load(f)

now = datetime.now(timezone.utc)
TIMEOUT_HARD = timedelta(seconds=5400)  # 90 min
TIMEOUT_SOFT = timedelta(seconds=1800)  # 30 min

status = "HEALTHY"

for agent in data.get('agents', []):
  agent_id = agent.get('id')
  heartbeat_str = agent.get('heartbeat_at')
  
  try:
    heartbeat = datetime.fromisoformat(heartbeat_str.replace('Z', '+00:00'))
    age = now - heartbeat
    
    if age > TIMEOUT_HARD:
      print(f"❌ CORRUPTED: {agent_id} - heartbeat timeout ({age.total_seconds()/60:.0f} min > 90 min)")
      status = "CORRUPTED"
    elif age > TIMEOUT_SOFT:
      print(f"⚠️ DEGRADED: {agent_id} - heartbeat stale ({age.total_seconds()/60:.0f} min old)")
      status = "DEGRADED"
    else:
      print(f"✅ HEALTHY: {agent_id} - heartbeat fresh ({age.total_seconds():.0f} sec old)")
  except:
    print(f"❌ CORRUPTED: {agent_id} - invalid timestamp format")
    status = "CORRUPTED"

if status is not "HEALTHY":
  exit(1 if status == "CORRUPTED" else 0)
EOF
}

check_agent_lock
```

### 1.4 Acciones Automáticas

```bash
# Si estado = CORRUPTED
→ Ejecutar: ai/scripts/cleanup_dead_agent.sh
→ Remover agente de agent_lock.yaml
→ Revertir archivos bloqueados a git HEAD

# Si estado = DEGRADED
→ Alertar a humano
→ Registrar en monitoring log
→ Continuar monitoreando
```

---

## ARCHIVO 2: `tasks.yaml`

### 2.1 Schema Esperado

```yaml
schema_version: "3.0"
min_compatible_version: "2.0"
last_updated: "{ISO8601}"
total_tasks: {num}
pending: {count}
in_progress: {count}
blocked: {count}
done: {count}

tasks:
  - id: "{TASK-{num}}"
    title: "{task title}"
    status: "pending|claimed|in_progress|review|done|cancelled"
    depends_on:
      - "{TASK-id}"
    blocks:
      - "{TASK-id}"
    assigned_agent: "{agent-id|null}"
    definition_of_done:
      - "{criterion}"
```

### 2.2 Matriz de Estados

| Estado | Condiciones | Acción |
|--------|-------------|--------|
| ✅ HEALTHY | Todos los `depends_on` referencias existen, sin ciclos, statuses válidas | NADA |
| ⚠️ DEGRADED | Orphan task sin blocker (referenciado pero no existe) | WARN + continue |
| ❌ CORRUPTED | Ciclo de dependencias (TASK-A → B → A), status inválido | FAIL |

### 2.3 Script de Validación

```bash
#!/bin/bash
# ai/scripts/validate_tasks.sh

FILE="ai/memory/tasks.yaml"

check_tasks() {
  echo "Validating: $FILE"
  
  # 1. YAML syntax
  if ! python3 -c "import yaml; yaml.safe_load(open('$FILE'))" 2>/dev/null; then
    echo "❌ CORRUPTED: Invalid YAML"
    return 1
  fi
  
  # 2. Task graph integrity
  python3 << 'EOF'
import yaml
import sys

with open('ai/memory/tasks.yaml') as f:
  data = yaml.safe_load(f)

tasks = {t['id']: t for t in data.get('tasks', [])}
valid_statuses = ['pending', 'claimed', 'in_progress', 'review', 'done', 'cancelled']
corrupted = False
degraded = False

# Verificar cada tarea
for task in data.get('tasks', []):
  task_id = task['id']
  
  # Verificar status válido
  if task.get('status') not in valid_statuses:
    print(f"❌ CORRUPTED: {task_id} has invalid status '{task.get('status')}'")
    corrupted = True
  
  # Verificar depends_on references
  for dep in task.get('depends_on', []):
    if dep not in tasks:
      print(f"⚠️ DEGRADED: {task_id} depends on non-existent {dep}")
      degraded = True
  
  # Verificar blocks references
  for blocked in task.get('blocks', []):
    if blocked not in tasks:
      print(f"⚠️ DEGRADED: {task_id} blocks non-existent {blocked}")
      degraded = True

# Detectar ciclos
def has_cycle(start, visited, rec_stack):
  visited.add(start)
  rec_stack.add(start)
  
  for blocked in tasks[start].get('blocks', []):
    if blocked not in visited:
      if has_cycle(blocked, visited, rec_stack):
        return True
    elif blocked in rec_stack:
      return True
  
  rec_stack.remove(start)
  return False

visited = set()
for task_id in tasks:
  if task_id not in visited:
    if has_cycle(task_id, visited, set()):
      print(f"❌ CORRUPTED: Dependency cycle detected involving {task_id}")
      corrupted = True

# Status counts
status_counts = {}
for task in data.get('tasks', []):
  status = task.get('status')
  status_counts[status] = status_counts.get(status, 0) + 1

# Verificar counts en header
for status in valid_statuses:
  expected = data.get(status, 0)
  actual = status_counts.get(status, 0)
  if expected != actual:
    print(f"⚠️ DEGRADED: Status count mismatch for '{status}': header={expected}, actual={actual}")
    # Aquí podría ser CORRUPTED si es muy grave
    if abs(expected - actual) > 5:
      corrupted = True

if corrupted:
  print("STATUS: CORRUPTED ❌")
  sys.exit(1)
elif degraded:
  print("STATUS: DEGRADED ⚠️")
  sys.exit(0)
else:
  print("STATUS: HEALTHY ✅")
  sys.exit(0)
EOF
}

check_tasks
```

### 2.4 Acciones Automáticas

```bash
# Si CORRUPTED
→ Bloquear todos los commits
→ Requerir intervención humana
→ Generar report detallado
→ Sugerir: git checkout HEAD -- ai/memory/tasks.yaml

# Si DEGRADED
→ Loguear warning
→ Permitir commits (con advertencia)
→ Humano debe saneár en próximas horas
```

---

## ARCHIVO 3: `signals.yaml`

### 3.1 Schema Esperado

```yaml
schema_version: "4.0"
min_compatible_version: "4.0"
last_updated: "{ISO8601}"

signals:
  - id: "{SIG-{num}}"
    type: "task_unblocked|review_requested|review_approved|warning|recovery|info"
    from: "{source_agent_or_system}"
    to: "{target_agent_or_any}"
    task_id: "{TASK-id}"
    message: "{human readable message}"
    created_at: "{ISO8601}"
    read_by:
      - { agent: "{agent_id}", at: "{ISO8601}" }
```

### 3.2 Matriz de Estados

| Estado | Condiciones | Acción |
|--------|-------------|--------|
| ✅ HEALTHY | Append-only, no duplicates, valid timestamps | NADA |
| ⚠️ DEGRADED | Duplicate SIG-ids, outdated signals (>7 days) | ARCHIVE old signals |
| ❌ CORRUPTED | Backwards timestamps (created_at > read_at), invalid task refs | REBUILD from git |

### 3.3 Script de Validación

```bash
#!/bin/bash
# ai/scripts/validate_signals.sh

FILE="ai/memory/signals.yaml"

check_signals() {
  echo "Validating: $FILE"
  
  if [ ! -f "$FILE" ]; then
    echo "⚠️ DEGRADED: File not found (optional)"
    return 0  # Signals pueden no existir
  fi
  
  python3 << 'EOF'
import yaml
from datetime import datetime, timedelta, timezone
import sys

with open('ai/memory/signals.yaml') as f:
  data = yaml.safe_load(f)

signals = data.get('signals', [])
corrupted = False
degraded = False

seen_ids = set()
now = datetime.now(timezone.utc)

for signal in signals:
  sig_id = signal.get('id')
  
  # Verificar ID único
  if sig_id in seen_ids:
    print(f"❌ CORRUPTED: Duplicate signal ID {sig_id}")
    corrupted = True
  seen_ids.add(sig_id)
  
  # Verificar timestamps válidos
  created_at = signal.get('created_at')
  try:
    created = datetime.fromisoformat(created_at.replace('Z', '+00:00'))
  except:
    print(f"❌ CORRUPTED: {sig_id} has invalid created_at timestamp")
    corrupted = True
    continue
  
  # Verificar que no es del futuro
  if created > now:
    print(f"❌ CORRUPTED: {sig_id} has future timestamp")
    corrupted = True
  
  # Verificar read_by timestamps si existen
  for read in signal.get('read_by', []):
    read_at = read.get('at')
    try:
      read_time = datetime.fromisoformat(read_at.replace('Z', '+00:00'))
      
      # read_at debe ser >= created_at
      if read_time < created:
        print(f"❌ CORRUPTED: {sig_id} read before created (backwards timestamp)")
        corrupted = True
    except:
      print(f"❌ CORRUPTED: {sig_id} has invalid read.at timestamp")
      corrupted = True
  
  # Verificar edad de la señal
  age = now - created
  if age > timedelta(days=7):
    print(f"⚠️ DEGRADED: {sig_id} is {age.days} days old (should be archived)")
    degraded = True

if corrupted:
  print("STATUS: CORRUPTED ❌")
  sys.exit(1)
elif degraded:
  print("STATUS: DEGRADED ⚠️")
  sys.exit(0)
else:
  print(f"STATUS: HEALTHY ✅ ({len(signals)} signals)")
  sys.exit(0)
EOF
}

check_signals
```

---

## ARCHIVO 4: `decisions.md`

### 4.1 Reglas de Formato

```markdown
# decisions.md — Formato esperado

## Sección por cada decisión:

### DEC-{id}: {título}

**Contexto:**
- Problema descripto
- Alternativas exploradas
- Restricciones

**Decisión:**
- Opción elegida
- Razón de la elección

**Consecuencias:**
- Impacto técnico
- Impacto en timeline
```

### 4.2 Matriz de Estados

| Estado | Condiciones | Acción |
|--------|-------------|--------|
| ✅ HEALTHY | Todas las DEC-{id} tienen las 3 secciones (Contexto/Decisión/Consecuencias) | NADA |
| ⚠️ DEGRADED | Falta alguna sección, o DEC incompleto | WARN + log |
| ❌ CORRUPTED | DEC-ids duplicados, formato markdown roto | MANUAL REVIEW |

### 4.3 Script de Validación

```bash
#!/bin/bash
# ai/scripts/validate_decisions.sh

FILE="ai/memory/decisions.md"

check_decisions() {
  echo "Validating: $FILE"
  
  if [ ! -f "$FILE" ]; then
    echo "⚠️ DEGRADED: File not found (optional)"
    return 0
  fi
  
  # Contar DEC-{id}
  dec_count=$(grep -c "^### DEC-" "$FILE" || echo "0")
  echo "Found $dec_count decisions"
  
  # Verificar que cada DEC tiene las 3 secciones obligatorias
  grep "^### DEC-" "$FILE" | while read line; do
    id=$(echo "$line" | sed 's/.*DEC-/DEC-/' | sed 's/:.*//')
    
    # Extraer sección para este DEC
    sed -n "/^### $id:/,/^### DEC-/p" "$FILE" | head -n -1 > /tmp/dec_content.txt
    
    if grep -q "^\*\*Contexto:" /tmp/dec_content.txt && \
       grep -q "^\*\*Decisión:" /tmp/dec_content.txt && \
       grep -q "^\*\*Consecuencias:" /tmp/dec_content.txt; then
      echo "✅ $id: Complete"
    else
      echo "⚠️ $id: Missing sections"
    fi
  done
  
  # Verificar DEC-ids duplicados
  dup_count=$(grep -c "^### DEC-" "$FILE" | wc -l)
  unique_count=$(grep "^### DEC-" "$FILE" | sort -u | wc -l)
  
  if [ $dup_count -ne $unique_count ]; then
    echo "❌ CORRUPTED: Duplicate DEC-ids detected"
    return 1
  fi
  
  echo "✅ HEALTHY: $dec_count unique decisions"
}

check_decisions
```

---

## ARCHIVO 5: `change_log.md`

### 5.1 Formato Esperado

```markdown
## [Timestamp] Cambio ID: CHG-{id}

**Tipo:** feature | bugfix | refactor | docs | recovery  
**Archivos:** [lista]  
**Motivo:** [descripción]  
**Reversión:** [instrucciones si aplica]  
```

### 5.2 Matriz de Estados

| Estado | Condiciones | Acción |
|--------|-------------|--------|
| ✅ HEALTHY | Append-only, timestamps cronológicos, formatos consistentes | NADA |
| ⚠️ DEGRADED | Timestamps out of order, formatos inconsistentes | SORT + continue |
| ❌ CORRUPTED | Backwards timestamps (entry_A > entry_B), missing critical info | REBUILD |

### 5.3 Script de Validación

```bash
#!/bin/bash
# ai/scripts/validate_change_log.sh

FILE="ai/memory/change_log.md"

check_change_log() {
  echo "Validating: $FILE"
  
  if [ ! -f "$FILE" ]; then
    echo "⚠️ DEGRADED: File not found"
    return 0
  fi
  
  # Verificar que es append-only (timestamps crecientes)
  python3 << 'EOF'
import re
from datetime import datetime

with open('ai/memory/change_log.md') as f:
  content = f.read()

# Extraer todos los timestamps
pattern = r'\[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z)\]'
timestamps = []

for match in re.finditer(pattern, content):
  ts_str = match.group(1)
  try:
    ts = datetime.fromisoformat(ts_str.replace('Z', '+00:00'))
    timestamps.append((ts_str, ts))
  except:
    print(f"❌ CORRUPTED: Invalid timestamp format {ts_str}")
    exit(1)

# Verificar orden cronológico
for i in range(1, len(timestamps)):
  if timestamps[i][1] < timestamps[i-1][1]:
    print(f"❌ CORRUPTED: Backwards timestamps: {timestamps[i-1][0]} > {timestamps[i][0]}")
    exit(1)

if len(timestamps) > 0:
  print(f"✅ HEALTHY: {len(timestamps)} entries in cronological order")
else:
  print("⚠️ DEGRADED: No entries found")
  exit(0)
EOF
}

check_change_log
```

---

## MATRIZ DE VALIDACIÓN GLOBAL

```bash
#!/bin/bash
# ai/scripts/validate_all_files.sh
#
# Ejecutar para verificar integridad del sistema completo

echo "=== WORKSPACE HEALTH CHECK ==="
echo ""

OVERALL_STATUS="HEALTHY"

# Ejecutar todas las validaciones
for validator in ai/scripts/validate_*.sh; do
  echo "Running: $(basename $validator)"
  
  if bash "$validator"; then
    echo "  → PASS ✅"
  else
    echo "  → FAIL/WARN ⚠️"
    
    # Si era crítico (agent_lock o tasks), marcar como degradado
    if [[ "$validator" =~ "agent_lock|tasks" ]]; then
      OVERALL_STATUS="DEGRADED"
    fi
  fi
  echo ""
done

echo "=== OVERALL STATUS: $OVERALL_STATUS ==="

if [ "$OVERALL_STATUS" == "HEALTHY" ]; then
  exit 0
elif [ "$OVERALL_STATUS" == "DEGRADED" ]; then
  exit 1  # Falla CI/CD
fi
```

---

## EJECUCIÓN AUTOMÁTICA

### En CI/CD:

```yaml
# .github/workflows/health-check.yml

jobs:
  health_check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: bash ai/scripts/validate_all_files.sh
```

### Pre-commit:

```bash
# .git/hooks/pre-commit
bash ai/scripts/validate_all_files.sh || exit 1
```

### Manual:

```bash
# Developer puede chequear en cualquier momento
cd ai-workspace-memory-system/
bash ai/scripts/validate_all_files.sh
```

---

## TABLA RESUMEN

| Archivo | Healthy | Degraded | Corrupted | Action |
|---------|---------|----------|-----------|--------|
| agent_lock.yaml | Vacío o pulsos < 30m | Pulso 30-90m | Pulso > 90m | AUTO-CLEANUP |
| tasks.yaml | Graph coherente | Orphan tasks | Ciclos/status inválido | MANUAL-REVIEW |
| signals.yaml | Append-only | Outdated signals | Backwards timestamps | REBUILD |
| decisions.md | Todas DEC completas | Falta sección | DEC duplicados | MANUAL-REVIEW |
| change_log.md | Cronológico | Formato inconsistente | Backwards timestamps | REBUILD |
| agent_profiles.yaml | All profiles valid | Unused profiles | Duplicate IDs | MANUAL-REVIEW |

---

## CONCLUSIÓN

**Sistema de validación automático que mantiene integridad sin intervención humana.**

- ✅ 5+ scripts de validación (uno por archivo)
- ✅ 3 niveles de estado (HEALTHY/DEGRADED/CORRUPTED)
- ✅ Acciones automáticas para cada estado
- ✅ Ejecutable en pre-commit, CI/CD, y manual
- ✅ Full determinism (mismos datos = mismo resultado)
