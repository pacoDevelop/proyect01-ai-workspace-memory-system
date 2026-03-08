# GIT WORKFLOW Y RESOLUCIÓN DE CONFLICTOS

> Última actualización: {YYYY-MM-DDTHH:MM:SSZ}

---

## Regla fundamental

Los archivos `/ai/` son infraestructura crítica compartida.
**SIEMPRE leer el estado más reciente ANTES de escribir.**

---

## Flujo por tipo de cambio

### Código del proyecto (src/, tests/, docs/, config/)

```bash
git checkout main && git pull --rebase origin main
# Si hay conflictos: resolver siguiendo tu estrategia de merge

# 1. Crear branch con nombre despriptivo
git checkout -b feat/TASK-{id}-{descripción-corta}

# 2. Trabajar y commitear normalmente
git add src/...
git commit -m "feat: {descripción} [TASK-{id}]"

# 3. Al terminar, push al origin
git push origin feat/TASK-{id}-{descripción-corta}

# 4. Pull request (si aplica en tu flujo) o:

# 5. Squash merge a main
git checkout main && git pull --rebase origin main
git merge --squash feat/TASK-{id}-{descripción-corta}
git commit -m "feat: {título de cambio} [TASK-{id}]"
git push origin main

# 6. Eliminar branch local y remoto
git branch -d feat/TASK-{id}-{descripción-corta}
git push origin --delete feat/TASK-{id}-{descripción-corta}
```

### Archivos /ai/ (context.md, tasks.yaml, signals.yaml, etc.)

⚠️  **SIEMPRE directo en main. Nunca en un branch de feature.**

```bash
git checkout main && git pull --rebase origin main
# Si hay conflictos: ver sección de resolución de conflictos

# 1. Editar el archivo
nano ai/tasks.yaml

# 2. Verificar que los cambios son correctos
git diff ai/tasks.yaml

# 3. Commit directo a main
git add ai/{archivo}
git commit -m "ai: {descripción} [TASK-{id}]"

# 4. Push inmediato
git push origin main
```

**Por qué directo en main:** Los archivos de control necesitan visibilidad inmediata
para todos los agentes. Un branch introduce latencia de estado que causa colisiones.

---

## Convenciones de commits

```
feat:       feat: add refresh token endpoint [TASK-047]
fix:        fix: token expiry not checked [TASK-048]
refactor:   refactor: extract auth validation [TASK-051]
test:       test: add integration tests for auth [TASK-047]
docs:       docs: update API documentation [TASK-050]
ai:         ai: task TASK-047 done, update context [TASK-047]
human:      human: update auth requirements doc
recovery:   recovery: clean ghost agent [RECOVERY]
merge:      merge: resolve tasks.yaml conflict [MERGE-RESOLUTION]
chore:      chore: update dependencies [MAINTENANCE]
```

---

## Resolución de conflictos en archivos /ai/

### Situación: Git reporta conflict en agent_lock.yaml, tasks.yaml, etc.

**Paso 1: NO usar merge automático**
```bash
git checkout --conflict=diff3 ai/tasks.yaml
# Esto mostrará las 3 versiones: base, ours, theirs
```

**Paso 2: Aplicar reglas de resolución campo por campo**
Para cada conflicto:
- Campo con `updated_at` **claro**: prevalece el timestamp más reciente
- Campo sin `updated_at`: prevalece la versión con más commits recientes
- Arrays (tasks, state_history): UNIÓN de ambas versiones sin duplicados

**Paso 3: Reglas ABSOLUTAS (nunca violar)**
- ✗ NUNCA eliminar una entrada de `state_history`
- ✗ NUNCA eliminar una entrada de `change_log.md`
- ✗ NUNCA eliminar una tarea íntegra (marcar `cancelled` si es necesario)
- ✓ SIEMPRE registrar la resolución en `change_log.md` con etiqueta `[MERGE-RESOLUTION]`

**Paso 4: Completar y subir**
```bash
git add ai/tasks.yaml
git commit -m "merge: resolve tasks.yaml conflict [MERGE-RESOLUTION]"
git push origin main
```

### Ejemplo de conflicto en tasks.yaml

```yaml
<<<<<<< HEAD (main actual)
  - id: "TASK-047"
    title: "Implementar refresh token"
    status: "in_progress"
    assigned_agent: "claude-agent-alpha"
    updated_at: "2025-03-07T10:00:00Z"
||||||| base (commit común)
  - id: "TASK-047"
    title: "Implementar refresh token"
    status: "pending"
    assigned_agent: null
    updated_at: "2025-03-06T15:00:00Z"
=======  (rama entrante)
  - id: "TASK-047"
    title: "Implementar refresh token endpoint"
    status: "claimed"
    assigned_agent: "claude-agent-beta"
    updated_at: "2025-03-07T09:30:00Z"
>>>>>>> rama-de-otro-agente
```

**Resolución:** El timestamp más reciente gana: `2025-03-07T10:00:00Z` (HEAD)
```yaml
  - id: "TASK-047"
    title: "Implementar refresh token"
    status: "in_progress"
    assigned_agent: "claude-agent-alpha"
    updated_at: "2025-03-07T10:00:00Z"
```

Luego registrar en change_log.md:
```markdown
## [2025-03-07 10:15Z] [MERGE-RESOLUTION] — {agente-que-resolvió}
**Cambio:** Resolvido conflicto en tasks.yaml para TASK-047
**Motivo:** Dos agentes modificaron task simultáneamente
**Resolución:** Aplicada regla: prevalece updated_at más reciente (HEAD)
```

---

## Constantes de branching

| Nombre | Propósito | Creado por | Merge hacia |
|--------|-----------|------------|------------|
| `main` | Rama estable, archivos /ai/ siempre aquí | Sistema | N/A (rama principal) |
| `feat/TASK-{id}-{desc}` | Feature de una tarea | Agente | main (squash merge) |
| `fix/TASK-{id}-{desc}` | Bugfix específico | Agente | main (squash merge) |
| `human/{desc}` | Trabajo humano sin tarea | Humano | main |

---

## CI/CD checks en every push

Los siguientes checks DEBEN ejecutarse automáticamente (pre-commit hook + GitHub Actions):

1. **Validación de integridad del workspace** → `bash ai/scripts/validate_workspace.sh`
2. **Escaneo de secretos** → detectar credenciales comprometidas antes de que lleguen a git
3. **SAST** (Semgrep, eslint-security, etc.) → análisis estático de código
4. **Tests unitarios** → npm test
5. **Auditoría de dependencias** → npm audit --audit-level=high

Si cualquier check falla → **COMMIT BLOQUEADO**. No puede llegar a main.

Ver Entregable 11 para configuración completa de CI/CD.
