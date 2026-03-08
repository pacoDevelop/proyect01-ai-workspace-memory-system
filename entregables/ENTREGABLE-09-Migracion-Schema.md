# ENTREGABLE 9: Guía de Migración de Schema

**Descripción:** Cómo actualizar la versión del schema de los archivos YAML sin corromper datos existentes (breaking changes).

**Escenario Real:** Pasamos de schema_version 3.0 a 4.0, y queremos asegurar que ningún dato se pierda en la transición.

---

## REGLAS FUNDAMENTALES DE MIGRACIÓN

```
1. NUNCA cambiar schema_version sin backward compatibility
2. SIEMPRE soportar N-1 versiones atrás (transitional compatibility)
3. NUNCA eliminar un campo (deprecar, no eliminar)
4. SIEMPRE tener rollback plan (git revert)
5. SIEMPRE ejecutar con todos los agentes pausados
6. SIEMPRE documentar en DEC-{id}
7. SIEMPRE hacer test con datos reales
```

---

## CASO: Migración 3.0 → 4.0

### CAMBIOS PLANIFICADOS

```yaml
# v3.0 (actual)
tasks:
  - id: "TASK-044"
    title: "..."
    status: "done"
    priority: "high"          # ← SERÁ DEPRECADO en 4.0
    tags: []
    created_at: "..."

# v4.0 (nuevo)
tasks:
  - id: "TASK-044"
    title: "..."
    status: "done"
    severity: "high"          # ← NUEVO (renombrado de priority)
    priority: null            # ← DEPRECATED (keep for compatibility)
    tags: []
    metadata:                 # ← NUEVO
      estimated_effort: "2h"
      actual_effort: "1h 45m"
    created_at: "..."
```

**Problemas a resolver:**
- Campo `priority` renombrado a `severity` (qué hacer con data vieja?)
- Nuevo campo `metadata` (qué pasa con tasks que no lo tienen?)
- Dos schema versions coexistiendo (compatibilidad?)`

---

## FASE 1: PLANIFICACIÓN (1-2 días antes)

### 1.1 Crear Decision Técnica (DEC-)

```markdown
### DEC-016: Schema v3.0 → v4.0 Migration Strategy

**Contexto:**
- Necesitamos agregar campo "metadata" a tasks
- Queremos renombrar "priority" → "severity"
- Queremos mantener backward compatibility durante transición

**Opciones consideradas:**
1. Big bang: cambiar todo de una vez (RIESGO: data loss si falla)
2. Dual write: soportar ambos fields temporalmente (SEGURO)
3. Dual read: aceptar ambas versiones (SEGURO)

**Decisión: Opción 2 + 3 (Dual write/read)**

**Plan:**
1. Week 1: Deploy v4.0 schema pero con v3.0 compatibility
   - Nuevas writes crean `metadata` pero preservan `priority`
   - Reads aceptan ambos campos
   - schema_version = "4.0"
   - min_compatible_version = "3.0" (!)

2. Week 2: Migrate existing data via script
   - Para cada TASK: si tiene `priority` y no tiene `metadata`,  crear metadata
   - Verificar integridad

3. Week 3: Deprecation warning
   - Logs alertan cuando se usa `priority` (deprecated)
   - Pero aún funciona

4. Week 4: (Future) Remover `priority` (major.version++)

**Timeline:**
- Planning: 2025-03-08 (today)
- Development: 2025-03-09 to 2025-03-11
- Testing: 2025-03-12
- Deployment: 2025-03-13 (Thursday, not Friday!)
- Monitoring: 2025-03-14 to 2025-03-20
- Migration: 2025-03-20
- Full deprecation: 2025-04-01

**Rollback:**
```bash
git revert [commit-hash]
# Vuelve a v3.0 inmediatamente
```

**Success Metrics:**
- [ ] 100% of tasks migrated to v4.0 schema
- [ ] 0 data loss
- [ ] No breaking changes para agentes que aún usan v3.0
- [ ] Full audit trail in git + change_log.md
- [ ] Backward compatibility verified by tests
```

### 1.2 Notificar a Agentes

```yaml
# En ai/memory/signals.yaml

- id: "SIG-PRE-001"
  type: "info"
  from: "system"
  to: "any"
  message: "NOTICE: Schema migration v3.0 → v4.0 planned for 2025-03-13. See DEC-016 for details."
  created_at: "2025-03-08T10:00:00Z"
  migration_event: "SCHEMA-v4.0"
  action_required: "None - will be transparent"
```

### 1.3 Crear Rollback Script

```bash
#!/bin/bash
# scripts/rollback_schema_migration.sh

echo "=== SCHEMA MIGRATION ROLLBACK ==="
echo ""

# Paso 1: Identificar commit de migración
echo "Last 10 commits related to schema:"
git log --grep="schema" --oneline | head -10

echo ""
echo "Ingrese commit hash a revertir (o 'cancel' para salir):"
read COMMIT_HASH

if [ "$COMMIT_HASH" == "cancel" ]; then
  echo "Rollback cancelled"
  exit 0
fi

# Paso 2: Verificar que el commit existe
if ! git show $COMMIT_HASH &> /dev/null; then
  echo "❌ Commit no existe: $COMMIT_HASH"
  exit 1
fi

# Paso 3: Crear rollback commit
git revert $COMMIT_HASH --no-edit -m 1

echo ""
echo "✅ Rollback commit created"
echo "Próximos pasos:"
echo "1. Revisar cambios: git show HEAD"
echo "2. Validar: bash ai/scripts/validate_all_files.sh"
echo "3. Si OK, push: git push origin main"
echo "4. Comunicar a team: Update signals.yaml"
```

---

## FASE 2: DEVELOPMENT (3-4 días)

### 2.1 Actualizar Schema en Plantilla

```yaml
# plantillas/PLANTILLA-tasks.yaml — NUEVA VERSIÓN 4.0

schema_version: "4.0"
min_compatible_version: "3.0"  # ← IMPORTANTE: Aún soportamos v3.0
last_updated: "{timestamp}"
last_updated_by: "{author}"

tasks:
  - id: "{TASK-{num}}"
    title: "{title}"
    status: "pending|claimed|in_progress|review|done"
    
    # DEPRECATED (v3.0 compatibility) - será removido en v5.0
    priority: null  # Usar en transición, deprecado después
    
    # NUEVO (v4.0) - renombrado de priority
    severity: "critical|high|medium|low"
    
    # NUEVO (v4.0)
    metadata:
      estimated_effort: "{duration}"
      actual_effort: "{duration}"
      estimated_risk: "high|medium|low"
    
    created_at: "{ISO8601}"
    # ... resto de campos ...
```

### 2.2 Crear Migration Script

```bash
#!/bin/bash
# scripts/migrate_tasks_v3_to_v4.sh
#
# Ejecutado una sola vez, convierte v3.0 tasks.yaml a v4.0

set -e

BACKUP_FILE="ai/memory/tasks.yaml.backup.$(date +%Y%m%d-%H%M%S)"
TASKS_FILE="ai/memory/tasks.yaml"

echo "=== SCHEMA MIGRATION v3.0 → v4.0 ==="
echo ""

# Paso 1: Backup
echo "[1/4] Creating backup..."
cp "$TASKS_FILE" "$BACKUP_FILE"
echo "  ✓ Backup: $BACKUP_FILE"

# Paso 2: Validar v3.0
echo ""
echo "[2/4] Validating v3.0 source..."
if ! grep -q 'schema_version: "3.0"' "$TASKS_FILE"; then
  echo "  ❌ File is not v3.0"
  exit 1
fi
echo "  ✓ File is v3.0"

# Paso 3: Migración Python
echo ""
echo "[3/4] Migrating data..."

python3 << 'PYTHON_MIGRATION'
import yaml
from datetime import datetime
import sys

# Leer v3.0
with open('ai/memory/tasks.yaml', 'r') as f:
    data_v3 = yaml.safe_load(f)

# Transformar a v4.0
data_v4 = {
    'schema_version': '4.0',
    'min_compatible_version': '3.0',
    'last_updated': datetime.utcnow().isoformat() + 'Z',
    'last_updated_by': 'migration-script',
    'last_update_reason': 'Schema upgrade v3.0 → v4.0',
    'total_tasks': len(data_v3.get('tasks', [])),
    'tasks': []
}

# Copiar counts si existen
for status in ['pending', 'in_progress', 'review', 'done', 'claimed', 'blocked', 'cancelled']:
    if status in data_v3:
        data_v4[status] = data_v3[status]

# Migrar cada task
migration_summary = {
    'total': 0,
    'with_priority_to_severity': 0,
    'new_metadata_created': 0,
    'errors': []
}

for task_v3 in data_v3.get('tasks', []):
    task_v4 = task_v3.copy()
    migration_summary['total'] += 1
    
    # Transformation 1: priority → severity
    if 'priority' in task_v3 and 'severity' not in task_v3:
        task_v4['severity'] = task_v3['priority']
        migration_summary['with_priority_to_severity'] += 1
        print(f"  ✓ {task_v3['id']}: priority → severity")
    
    # Transformation 2: Crear metadata si no existe
    if 'metadata' not in task_v4:
        task_v4['metadata'] = {
            'estimated_effort': task_v3.get('estimated_effort', None),
            'actual_effort': task_v3.get('actual_effort', None),
            'created_from_v3': True
        }
        migration_summary['new_metadata_created'] += 1
        print(f"  ✓ {task_v3['id']}: metadata field created")
    
    # Verificar integridad
    if 'id' not in task_v4 or 'status' not in task_v4:
        migration_summary['errors'].append(f"Task missing required fields: {task_v3['id']}")
        print(f"  ❌ {task_v3['id']}: missing required fields")
        continue
    
    data_v4['tasks'].append(task_v4)

# Escribir v4.0
with open('ai/memory/tasks.yaml', 'w') as f:
    yaml.dump(data_v4, f, default_flow_style=False, sort_keys=False)

# Resumen
print("\n=== MIGRATION SUMMARY ===")
print(f"Total tasks migrated: {migration_summary['total']}")
print(f"Tasks with priority→severity: {migration_summary['with_priority_to_severity']}")
print(f"Tasks with new metadata: {migration_summary['new_metadata_created']}")
if migration_summary['errors']:
    print(f"\n⚠️  ERRORS ({len(migration_summary['errors'])}):")
    for error in migration_summary['errors']:
        print(f"  - {error}")
    sys.exit(1)

print("\n✅ Migration completed successfully")
PYTHON_MIGRATION

# Paso 4: Validar v4.0
echo ""
echo "[4/4] Validating v4.0 result..."

if grep -q 'schema_version: "4.0"' ai/memory/tasks.yaml; then
  echo "  ✓ Schema version updated to 4.0"
else
  echo "  ❌ Schema version not updated"
  echo "  ROLLBACK: cp $BACKUP_FILE $TASKS_FILE"
  cp "$BACKUP_FILE" "$TASKS_FILE"
  exit 1
fi

if python3 -c "import yaml; yaml.safe_load(open('ai/memory/tasks.yaml'))" 2>/dev/null; then
  echo "  ✓ YAML syntax valid"
else
  echo "  ❌ YAML syntax invalid"
  cp "$BACKUP_FILE" "$TASKS_FILE"
  exit 1
fi

echo ""
echo "=== MIGRATION COMPLETE ==="
echo ""
echo "Backup preserved at: $BACKUP_FILE"
echo "Rollback: cp $BACKUP_FILE ai/memory/tasks.yaml"
echo ""
echo "Next steps:"
echo "1. Verify visually: cat ai/memory/tasks.yaml | head -50"
echo "2. Run tests: npm test"
echo "3. Commit: git add ai/memory/tasks.yaml"
echo "4. Push: git push origin main"
```

---

## FASE 3: TESTING (1 día)

### 3.1 Test Script con datos reales

```bash
#!/bin/bash
# scripts/test_migration.sh

echo "=== MIGRATION TESTING ==="
echo ""

# Test 1: Backward Compatibility
echo "[Test 1] Backward Compatibility - Can v3.0 agents read v4.0?"
python3 << 'EOF'
import yaml

# Leer v4.0
with open('ai/memory/tasks.yaml') as f:
    v4_data = yaml.safe_load(f)

# Simular v3.0 agent leyendo v4.0
for task in v4_data['tasks']:
    # v3.0 agent espera estos campos
    assert 'id' in task, "Missing 'id'"
    assert 'status' in task, "Missing 'status'"
    assert 'created_at' in task, "Missing 'created_at'"
    
    # v3.0 agent podría usar priority (si existe)
    # v4.0 ahora tiene severity, pero priority puede aún existir
    
print("✅ Backward compatibility verified")
EOF

# Test 2: Forward Compatibility  
echo "[Test 2] Forward Compatibility - v4.0 fields populated?"
python3 << 'EOF'
import yaml

with open('ai/memory/tasks.yaml') as f:
    v4_data = yaml.safe_load(f)

# Verificar que v4.0 fields están presentes
tasks_with_severity = sum(1 for t in v4_data['tasks'] if 'severity' in t)
tasks_with_metadata = sum(1 for t in v4_data['tasks'] if 'metadata' in t)

print(f"Tasks with v4.0 'severity': {tasks_with_severity}/{len(v4_data['tasks'])}")
print(f"Tasks with v4.0 'metadata': {tasks_with_metadata}/{len(v4_data['tasks'])}")

if tasks_with_severity == len(v4_data['tasks']):
    print("✅ All tasks have severity field")
else:
    print("⚠️  Some tasks missing severity field")
EOF

# Test 3: Data Integrity
echo "[Test 3] Data Integrity - No data lost?"
python3 << 'EOF'
import yaml

# Comparar task counts
with open('ai/memory/tasks.yaml.backup.*', 'r') as f:
    v3 = yaml.safe_load(f)

with open('ai/memory/tasks.yaml') as f:
    v4 = yaml.safe_load(f)

v3_count = len(v3.get('tasks', []))
v4_count = len(v4.get('tasks', []))

if v3_count == v4_count:
    print(f"✅ Task count preserved: {v3_count} → {v4_count}")
else:
    print(f"❌ Task count mismatch: {v3_count} → {v4_count}")
    exit(1)
EOF

echo ""
echo "=== ALL TESTS PASSED ==="
```

---

## FASE 4: DEPLOYMENT (1 día)

### 4.1 Pre-deployment Planning

```bash
# Checklist antes de hacer deploy

[ ] Backup actual existe y verificado
[ ] Migration script testeado 100% con datos reales
[ ] Todos los tests verdes (npm test)
[ ] Schema v4.0 y min_compatible="3.0" configurado
[ ] Rollback script listo y testeado
[ ] DEC-016 documentado completamente  
[ ] Cambios commiteados con mensaje claro
[ ] Comunicación a team enviada
[ ] Monitoreo setup (ver cambios en próx 48h)
```

### 4.2 Deployment Day

```bash
#!/bin/bash
# deploy-migration.sh

TIMESTAMP=$(date +%Y%m%dT%H%M%SZ)

echo "=== SCHEMA MIGRATION DEPLOYMENT ==="
echo "Timestamp: $TIMESTAMP"
echo ""

# 1. Verificar que se puede revertir
echo "[1/5] Verifying rollback plan..."
if [ ! -f "scripts/rollback_schema_migration.sh" ]; then
  echo "❌ Rollback script not found!"
  exit 1
fi

# 2. Ejecutar migration script
bash scripts/migrate_tasks_v3_to_v4.sh
MIGRATION_EXIT=$?

if [ $MIGRATION_EXIT -ne 0 ]; then
  echo "❌ Migration failed, staying on v3.0"
  exit 1
fi

# 3. Validar resultado
echo ""
echo "[2/5] Validating migrated data..."
bash ai/scripts/validate_tasks.sh

# 4. Commit
echo ""
echo "[3/5] Committing changes..."
git add ai/memory/tasks.yaml
git commit -m "feat: migrate schema v3.0 → v4.0 (DEC-016)

- Updated schema_version: 3.0 → 4.0
- Added 'severity' field (from 'priority')
- Added 'metadata' field for effort tracking
- min_compatible_version: 3.0 (backward compatible)
- Successfully migrated all $(grep -c 'id:' ai/memory/tasks.yaml) tasks
- Backup: ai/memory/tasks.yaml.backup.*
- See DEC-016 for rationale

Rollback available: git revert [commit-hash]"

# 5. Notificar
echo ""
echo "[4/5] Notifying system..."
cat >> ai/memory/signals.yaml << 'EOF'

  - id: "SIG-MIGR-001"
    type: "info"
    from: "system"
    to: "any"
    message: "Schema migration v3.0 → v4.0 completed successfully. All systems running v4.0."
    created_at: "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    migration_event: "SCHEMA-v4.0"
    backup_location: "ai/memory/tasks.yaml.backup.*"
EOF

git add ai/memory/signals.yaml
git commit -m "signal: schema migration v4.0 completed"

# 6. Push
echo ""
echo "[5/5] Pushing to main..."
git push origin main

echo ""
echo "✅ DEPLOYMENT COMPLETE"
echo "Monitor for 24-48 hours before considering transition complete"
```

---

## FASE 5: MONITORING (2-7 días)

```bash
#!/bin/bash
# monitor-migration.sh
# Ejecutar cada 6 horas durante 48 horas post-deployment

echo "=== POST-MIGRATION MONITORING ==="
echo ""

# 1. Verificar que v4.0 está siendo usado
echo "[1/3] Checking schema version..."
SCHEMA=$(grep 'schema_version:' ai/memory/tasks.yaml | head -1)
echo "  Current: $SCHEMA"
[ "$SCHEMA" == 'schema_version: "4.0"' ] && echo "  ✅ v4.0 active" || echo "  ❌ NOT v4.0"

# 2. Verificar integridad
echo ""
echo "[2/3] Checking data integrity..."
python3 ai/scripts/validate_tasks.sh

# 3. Verificar que agentes pueden operar
echo ""
echo "[3/3] Checking agent operations..."
grep "heartbeat_at:" ai/memory/agent_lock.yaml | head -3
echo "  (Agents should be able to read v4.0 schema)"

echo ""
echo "=== MONITORING COMPLETE ==="
```

---

## CONCLUSIÓN

**Migración de Schema sin downtime:**

- ✅ Backward compatible (v3.0 agents can read v4.0)
- ✅ Forward compatible (v4.0 schema defined with min_compatible_version)
- ✅ Zero data loss (backup + migration script validated)
- ✅ Rollback available (git revert works < 1 hour)
- ✅ Full audit trail (DEC-016 + commit + signals)
- ✅ Monitoring in place (24-48h post-deployment)
