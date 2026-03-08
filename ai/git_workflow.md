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
- `feat:` new feature
- `fix:` bug fix
- `ai:` changes to /ai/ workspace files
- `human:` human contribution
- `[MERGE-RESOLUTION]:` resolved merge conflict
