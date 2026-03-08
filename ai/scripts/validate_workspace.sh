#!/bin/bash
# AI Workspace Integrity Check

set -e
ERRORS=0
WARNINGS=0
AI_DIR="ai"

echo "▸ Validando integridad del workspace..."

# Verificar archivos obligatorios
REQUIRED=(
  "context.md" "agent_lock.yaml" "tasks.yaml" "signals.yaml"
  "change_log.md" "decisions.md" "git_workflow.md"
  "knowledge/overview.md" "memory/_index.md"
)

for f in "${REQUIRED[@]}"; do
  if [ ! -f "$AI_DIR/$f" ]; then
    echo "  ✗ FALTA: /ai/$f"
    ERRORS=$((ERRORS+1))
  fi
done

# Verificar schema_version en YAML files
for yaml_file in "$AI_DIR"/*.yaml; do
  if [ -f "$yaml_file" ]; then
    if ! grep -q "schema_version" "$yaml_file"; then
      echo "  ⚠️  $(basename $yaml_file) sin schema_version"
      WARNINGS=$((WARNINGS+1))
    fi
  fi
done

echo ""
if [ $ERRORS -eq 0 ]; then
  echo "✅ Workspace válido"
  exit 0
else
  echo "✗ Workspace requiere atención ($ERRORS errores)"
  exit 1
fi
