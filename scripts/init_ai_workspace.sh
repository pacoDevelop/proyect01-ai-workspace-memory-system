#!/bin/bash

# ═══════════════════════════════════════════════════════════════════════════════
# ENTREGABLE 5: init_ai_workspace.sh
# 
# Propósito: Inicializar un nuevo workspace AI en un repositorio.
#           Crea estructura completa de carpetas y archivos plantilla.
#
# Uso:
#   bash ai/scripts/init_ai_workspace.sh
#   # O desde raíz del repo:
#   bash ai/scripts/init_ai_workspace.sh
#
# Requisitos:
#   - Bash 4.0+
#   - Git (debe estar en un repositorio git)
#   - Permisos de escritura en directorio actual
#
# Output:
#   - Estructura /ai/ completamente creada
#   - Todas las plantillas con schema_version inicializado
#   - Validación automática del estado final
#   - Archivo SETUP_COMPLETE.log con timestamp
#
# ═══════════════════════════════════════════════════════════════════════════════

set -e  # Exit on error

# Colors para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Timestamps y directorios
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
INIT_LOG="ai/SETUP_COMPLETE.log"
AI_DIR="ai"

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  AI WORKSPACE INITIALIZATION v3.0${NC}"
echo -e "${BLUE}  $(date -u +"%Y-%m-%d %H:%M:%S UTC")${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# ── Verificación de requisitos ──────────────────────────────────────────────

echo -e "${YELLOW}▸ [0/8] Verificando requisitos...${NC}"

# Verificar que estamos en un repositorio git
if [ ! -d ".git" ]; then
  echo -e "${RED}✗ No estamos en un repositorio git.${NC}"
  echo "  Usa: git init && git remote add origin <repo-url>"
  exit 1
fi
echo -e "${GREEN}✓ En repositorio git${NC}"

# Verificar bash version
BASH_VERSION_NUM=$(echo $BASH_VERSION | cut -d. -f1)
if [ "$BASH_VERSION_NUM" -lt 4 ]; then
  echo -e "${RED}✗ Bash 4.0+ requerido. Tu versión: $BASH_VERSION${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Bash $BASH_VERSION${NC}"

# ── Crear estructura de directorios ─────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [1/8] Creando estructura de directorios...${NC}"

DIRS=(
  "ai"
  "ai/knowledge"
  "ai/memory"
  "ai/sessions"
  "ai/scripts"
  "ai/archive"
  "ai/archive/sessions"
  "ai/archive/memory"
)

for dir in "${DIRS[@]}"; do
  mkdir -p "$dir"
  echo -e "  ${GREEN}✓${NC} $dir"
done

# ── Crear archivos principales ──────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [2/8] Creando archivos principales...${NC}"

# context.md
cat > "$AI_DIR/context.md" << 'EOF'
# PROJECT CONTEXT
> Última actualización: {TIMESTAMP} | Actualizado por: {agent-name}
> ℹ️  Este workspace fue inicializado automáticamente. Actualizar esta plantilla.

## ▸ QUÉ ES ESTE PROYECTO
{Descripción de 2-3 líneas. Propósito, usuario objetivo, valor que entrega.}

## ▸ OBJETIVO ACTUAL
{Una sola frase. ¿Hacia dónde va el proyecto esta semana/sprint?}

## ▸ ESTADO DEL SISTEMA
**Estado:** STABLE
**Motivo:** Workspace freshly initialized.

## ▸ TAREAS PRIORITARIAS AHORA
{Actualizar con tareas reales una vez que crees la primera tarea}

## ▸ AGENTES ACTIVOS
{Actualizar cuando los agentes comienzan a trabajar}

## ▸ ARCHIVOS CRÍTICOS
{Actualizar si hay archivos que no deben ser modificados sin revisión}

## ▸ REGLAS DE ESTE PROYECTO
1. {Regla específica de este proyecto}
2. {Otra regla}

## ▸ LECTURAS RECOMENDADAS SEGÚN TAREA
{Guía rápida sobre qué archivos leer según el tipo de tarea}
EOF

sed -i "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/context.md" 2>/dev/null || \
  sed -i "" "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/context.md"

echo -e "  ${GREEN}✓${NC} context.md"

# agent_lock.yaml
cat > "$AI_DIR/agent_lock.yaml" << 'EOF'
# AGENT LOCK — Mutex distribuido
schema_version: "3.0"
min_compatible_version: "2.0"
last_updated: "{TIMESTAMP}"
last_updated_by: "init-script"
active_agents: []
ghost_entries: []
EOF

sed -i "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/agent_lock.yaml" 2>/dev/null || \
  sed -i "" "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/agent_lock.yaml"

echo -e "  ${GREEN}✓${NC} agent_lock.yaml"

# agent_profiles.yaml
cat > "$AI_DIR/agent_profiles.yaml" << 'EOF'
# AGENT PROFILES
schema_version: "5.0"
min_compatible_version: "4.0"
last_updated: "{TIMESTAMP}"
last_updated_by: "init-script"
agents:
  # Añadir tus agentes aquí:
  # - id: "agent-name"
  #   description: "Descripción del agente"
  #   specialization_tags: [domain1, domain2]
  #   max_task_priority: "critical"
  #   active: true
deactivated_agents: []
EOF

sed -i "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/agent_profiles.yaml" 2>/dev/null || \
  sed -i "" "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/agent_profiles.yaml"

echo -e "  ${GREEN}✓${NC} agent_profiles.yaml"

# tasks.yaml
cat > "$AI_DIR/tasks.yaml" << 'EOF'
# TASK GRAPH
schema_version: "3.0"
min_compatible_version: "2.0"
last_updated: "{TIMESTAMP}"
total_tasks: 0
pending: 0
in_progress: 0
blocked: 0
done: 0
tasks: []
EOF

sed -i "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/tasks.yaml" 2>/dev/null || \
  sed -i "" "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/tasks.yaml"

echo -e "  ${GREEN}✓${NC} tasks.yaml"

# signals.yaml
cat > "$AI_DIR/signals.yaml" << 'EOF'
# SIGNALS
schema_version: "4.0"
min_compatible_version: "4.0"
last_updated: "{TIMESTAMP}"
signals: []
EOF

sed -i "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/signals.yaml" 2>/dev/null || \
  sed -i "" "s/{TIMESTAMP}/$TIMESTAMP/g" "$AI_DIR/signals.yaml"

echo -e "  ${GREEN}✓${NC} signals.yaml"

# change_log.md
cat > "$AI_DIR/change_log.md" << 'EOF'
# CHANGE LOG

> Append-only. Never edit past entries. Start adding entries here:

EOF

echo -e "  ${GREEN}✓${NC} change_log.md"

# decisions.md
cat > "$AI_DIR/decisions.md" << 'EOF'
# ARCHITECTURE DECISIONS

> Record of technical decisions. Accepted decisions are immutable.
> New decisions are created as superseding if requirements change.

EOF

echo -e "  ${GREEN}✓${NC} decisions.md"

# git_workflow.md
cat > "$AI_DIR/git_workflow.md" << 'EOF'
# GIT WORKFLOW

## Regla fundamental
Los archivos /ai/ forman la infraestructura compartida.
Siempre leer el estado más reciente ANTES de escribir.

## Flujo de trabajo

### Para código del proyecto (src/, tests/):
```bash
git checkout main && git pull --rebase origin main
git checkout -b feat/TASK-{id}-{desc}
# ... trabaja ...
git commit -m "feat: ..."
git push origin feat/TASK-{id}-{desc}
# Merge a main (squash)
```

### Para archivos /ai/:
```bash
git checkout main && git pull --rebase origin main
# ... edita archivos /ai/ ...
git add ai/
git commit -m "ai: [descripción] [TASK-{id}]"
git push origin main
```

## Convenciones de commits
- feat: new feature
- fix: bug fix
- ai: changes to /ai/ workspace files
- human: human contribution
- [MERGE-RESOLUTION]: resolved merge conflict
```

echo -e "  ${GREEN}✓${NC} git_workflow.md"

# ideas.md
cat > "$AI_DIR/ideas.md" << 'EOF'
# IDEAS BACKLOG

> Parking lot de ideas no maduras. Máximo 20 ideas activas.

EOF

echo -e "  ${GREEN}✓${NC} ideas.md"

# ── Crear subdirectorios ───────────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [3/8] Creando archivos en subdirectorios...${NC}"

# knowledge files
for file in overview architecture repo_map glossary tech_stack; do
  cat > "$AI_DIR/knowledge/${file}.md" << EOF
# ${file^^}

> Última actualización: $TIMESTAMP
> Parte de la base de conocimiento del proyecto.

## Contenido
{Añadir contenido aquí}
EOF
  echo -e "  ${GREEN}✓${NC} knowledge/$file.md"
done

# memory shard index
cat > "$AI_DIR/memory/_index.md" << EOF
# MEMORY SHARDS INDEX

> Actualizar cuando añadas o modifiques shards.

| Shard | Relevancia | TL;DR | Última actualización |
|-------|-----------|-------|---------------------|

EOF

echo -e "  ${GREEN}✓${NC} memory/_index.md"

# ── Crear scripts ──────────────────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [4/8] Instalando scripts de validación...${NC}"

# validate_workspace.sh (versión simplificada para bootstrap)
cat > "$AI_DIR/scripts/validate_workspace.sh" << 'EOF'
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
EOF

chmod +x "$AI_DIR/scripts/validate_workspace.sh"
echo -e "  ${GREEN}✓${NC} scripts/validate_workspace.sh"

# ── Crear .gitignore para /ai/archive ──────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [5/8] Configurando .gitignore...${NC}"

if [ ! -f "$AI_DIR/.gitignore" ]; then
  cat > "$AI_DIR/.gitignore" << 'EOF'
# Los archivos /ai/ son parte del repositorio y se commitean
# Pero archive/ contiene backups de archivos archivados (históricos)
archive/sessions/*.md
archive/*.log

# Archivos temporales
*.swp
*.swo
*~
.DS_Store
EOF

  echo -e "  ${GREEN}✓${NC} .gitignore creado"
else
  echo -e "  ${YELLOW}→${NC} .gitignore ya existe, no modificando"
fi

# ── Crear archivo de setup log ─────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [6/8] Registrando inicialización...${NC}"

cat > "$INIT_LOG" << EOF
╔═════════════════════════════════════════════════════════════════╗
║        AI WORKSPACE INITIALIZATION LOG                          ║
╚═════════════════════════════════════════════════════════════════╝

Timestamp: $TIMESTAMP
Initialized by: init_ai_workspace.sh v3.0

ARCHIVOS CREADOS:
$(find "ai" -type f -newer "$INIT_LOG" 2>/dev/null | head -20 || echo "  (ver lista arriba)")

PRÓXIMOS PASOS:

1. CONFIGURAR AGENTES
   Editar: ai/agent_profiles.yaml
   Añadir lista de agentes autorizados con sus IDs

2. CREAR PRIMERA TAREA
   Editar: ai/tasks.yaml
   Añadir primera tarea con TASK-001

3. LLENAR CONTEXTO DEL PROYECTO
   Editar: ai/context.md
   Actualizar: QUÉ ES, OBJETIVO ACTUAL, ESTADO, REGLAS

4. CONOCIMIENTO BASE
   Editar: ai/knowledge/*.md
   Llenar overview, architecture, repo_map según proyecto

5. INITIALIZAR GIT
   bash:
     git add ai/
     git commit -m "chore: initialize AI workspace [SETUP]"
     git push origin main

6. VALIDAR
   bash:
     bash ai/scripts/validate_workspace.sh

DOCUMENTACIÓN COMPLETA:
  Ver /ai/git_workflow.md para protocolo de trabajo
  Ver especificación v6.0 FINAL para detalles complet

═════════════════════════════════════════════════════════════════
EOF

echo -e "  ${GREEN}✓${NC} $INIT_LOG"

# ── Validación final ────────────────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [7/8] Ejecutando validación...${NC}"

bash "$AI_DIR/scripts/validate_workspace.sh"
VALIDATION_RESULT=$?

if [ $VALIDATION_RESULT -eq 0 ]; then
  echo -e "  ${GREEN}✓ Validación exitosa${NC}"
else
  echo -e "  ${YELLOW}⚠ Validación con warnings${NC}"
fi

# ── Resumen final ──────────────────────────────────────────────────────────

echo ""
echo -e "${YELLOW}▸ [8/8] Resumen de inicialización${NC}"
echo ""
echo -e "${GREEN}✅ WORKSPACE INICIALIZADO CORRECTAMENTE${NC}"
echo ""
echo -e "Estructura creada:"
echo -e "  ${BLUE}ai/${NC}"
echo -e "  ├── context.md (LECTURA OBLIGATORIA)"
echo -e "  ├── agent_lock.yaml (mutex distribuido)"
echo -e "  ├── agent_profiles.yaml (agentes autorizados) ← LLENAR"
echo -e "  ├── tasks.yaml (grafo de tareas)"
echo -e "  ├── signals.yaml (notificaciones inter-agente)"
echo -e "  ├── change_log.md (registro de cambios)"
echo -e "  ├── decisions.md (decisiones técnicas permanentes)"
echo -e "  ├── git_workflow.md (protocolo de git)"
echo -e "  ├── ideas.md (ideas no maduras)"
echo -e "  ├── knowledge/ (base de conocimiento)"
echo -e "  ├── memory/ (memoria de agente)"
echo -e "  ├── sessions/ (registros de sesiones de agentes)"
echo -e "  ├── scripts/ (herramientas de validación)"
echo -e "  └── archive/ (almacenamiento histórico)"
echo ""
echo -e "${BLUE}PRÓXIMOS PASOS:${NC}"
echo -e "  1. Editar ${BLUE}ai/agent_profiles.yaml${NC} — añade tus agentes"
echo -e "  2. Editar ${BLUE}ai/context.md${NC} — describe el proyecto"
echo -e "  3. Ejecutar: ${YELLOW}bash ai/scripts/validate_workspace.sh${NC}"
echo -e "  4. Hacer git commit de los cambios"
echo ""
echo -e "${BLUE}DOCUMENTACIÓN:${NC}"
echo -e "  Log de setup: ${BLUE}$INIT_LOG${NC}"
echo -e "  Workflow: ${BLUE}ai/git_workflow.md${NC}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
