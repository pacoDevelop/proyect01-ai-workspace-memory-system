# PROTOCOL.md — PROTOCOLO OBLIGATORIO DE SESIÓN

> ⛔ **REGLA SUPREMA:** Este archivo NO es documentación opcional.
> Es el protocolo que ejecutas paso a paso, en orden, sin saltarte nada.
> **Cada gate marcado con 🔴 BLOQUEANTE significa: si no lo haces, PARA y hazlo ahora.**
> No puedes avanzar a la siguiente fase sin completar todos los pasos de la fase actual.

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## FASE 0 — INICIO DE SESIÓN
## *(ejecutar UNA VEZ al empezar, antes de tocar cualquier tarea)*
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### Paso 0.1 — Sincronizar estado
```
git pull --rebase origin main
```

### Paso 0.2 — Leer estado actual del proyecto
Leer en este orden exacto:
1. `/ai/context.md` — estado general del proyecto
2. `/ai/signals.yaml` — notificaciones pendientes para ti o "any"
3. `/ai/agent_lock.yaml` — qué agentes están activos ahora

### Paso 0.3 — Procesar señales
Para cada señal en `signals.yaml` donde `to == "any"` o `to == {tu-agent-id}`:
- Si no estás en `read_by`: procesarla y añadirte a `read_by`
- Commit inmediato: `git commit -m "ai: mark signals read [SESSION-START]"`

### 🔴 GATE 0 — BLOQUEANTE
Antes de continuar, confirma mentalmente:
- [ ] He leído context.md
- [ ] He leído signals.yaml y procesado señales pendientes
- [ ] He leído agent_lock.yaml y sé qué agentes están activos
- [ ] El estado del proyecto NO es `FROZEN` (si lo es: PARA, escala al humano)

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## FASE 1 — RECLAMAR UNA TAREA
## *(ejecutar cada vez que vas a empezar a trabajar en algo)*
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### Paso 1.1 — Verificar que no tienes tarea activa
Leer `agent_lock.yaml`. Si ya tienes una entrada con `status: working`:
- **PARA. Termina esa tarea primero.**
- Regla absoluta: máximo 1 tarea activa por agente.

### Paso 1.2 — Seleccionar tarea
Leer `/ai/tasks.yaml`. Filtrar en este orden:
1. Solo tareas con `status: pending` y `assigned_agent: null`
2. Eliminar las que tienen `depends_on` con tareas no en estado `done`
3. Ordenar: `critical` > `escalated` > `high` > `medium` > `low`
4. Elegir la primera de la lista filtrada y ordenada

### Paso 1.3 — Identificar archivos que vas a tocar
Antes de reclamar, decide qué archivos necesitarás modificar.
Revisa `agent_lock.yaml`: si alguno de esos archivos está en `locked_files` de otro agente activo:
- **PARA. Ese archivo está reservado. Espera o elige otra tarea.**

### 🔴 GATE 1A — BLOQUEANTE: Actualizar tasks.yaml
**AHORA, antes de escribir una sola línea de código:**

Editar `/ai/tasks.yaml`, en la tarea elegida:
```yaml
status: "claimed"              # cambiar de pending a claimed
assigned_agent: "{tu-agent-id}"
claimed_at: "{timestamp-ahora}"
updated_at: "{timestamp-ahora}"
state_history:
  - {from: "pending", to: "claimed", at: "{timestamp-ahora}", by: "{tu-agent-id}"}
```
Commit inmediato:
```
git add ai/tasks.yaml
git commit -m "ai: claim TASK-{id} [TASK-{id}]"
git push origin main
```

### 🔴 GATE 1B — BLOQUEANTE: Registrarse en agent_lock.yaml
**AHORA, inmediatamente después del paso anterior:**

Añadir tu entrada en `/ai/agent_lock.yaml` bajo `active_agents`:
```yaml
- agent_id: "{tu-agent-id}"
  session_id: "{YYYYMMDD-HHMMSS-tu-nombre}"
  started_at: "{timestamp-ahora}"
  last_heartbeat: "{timestamp-ahora}"
  current_task: "TASK-{id}"
  status: "working"
  working_branch: "feat/TASK-{id}-{descripcion-corta}"   # o main si es archivo /ai/
  base_branch: "main"
  locked_files:
    - "{path/archivo1-que-vas-a-tocar}"
    - "{path/archivo2-que-vas-a-tocar}"
  notes: "{Una línea de qué estás haciendo}"
```
Commit inmediato:
```
git add ai/agent_lock.yaml
git commit -m "ai: register agent lock TASK-{id} [TASK-{id}]"
git push origin main
```

### 🔴 GATE 1C — BLOQUEANTE: Cambiar tarea a in_progress
Editar `/ai/tasks.yaml`, en la tarea elegida:
```yaml
status: "in_progress"
started_at: "{timestamp-ahora}"
updated_at: "{timestamp-ahora}"
state_history:
  # añadir al array existente:
  - {from: "claimed", to: "in_progress", at: "{timestamp-ahora}", by: "{tu-agent-id}"}
```
Commit:
```
git add ai/tasks.yaml
git commit -m "ai: start TASK-{id} [TASK-{id}]"
git push origin main
```

### Paso 1.4 — Crear sesión
Crear archivo `/ai/sessions/{YYYYMMDD-HHMMSS-tu-nombre}.md` usando la plantilla `PLANTILLA-session.md`.
Rellenar: agente, tarea, inicio, objetivo de la sesión, contexto inicial.

### Paso 1.5 — Crear branch (solo para código, NO para archivos /ai/)
```
git checkout -b feat/TASK-{id}-{descripcion-corta}
```
*(Si la tarea solo toca archivos en /ai/, trabajar directamente en main)*

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## FASE 2 — DURANTE EL TRABAJO
## *(ejecutar mientras trabajas en la tarea)*
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### Paso 2.1 — Heartbeat cada ~15 minutos
Actualizar en `/ai/agent_lock.yaml`:
```yaml
last_heartbeat: "{timestamp-ahora}"
```
Commit:
```
git add ai/agent_lock.yaml
git commit -m "ai: heartbeat TASK-{id} [TASK-{id}]"
git push origin main
```
> Si llevas más de 15 minutos sin actualizar el heartbeat, hazlo ahora antes de continuar.

### Paso 2.2 — Si descubres conocimiento nuevo
Actualizar el shard relevante en `/ai/memory/` o crear uno nuevo.
Actualizar `/ai/memory_index.md` si creaste un shard nuevo.

### Paso 2.3 — Si tomas una decisión técnica importante
Documentarla en `/ai/decisions.md` con el formato DEC-{id}.

### Paso 2.4 — Si la tarea queda bloqueada
Editar `/ai/tasks.yaml`:
```yaml
status: "blocked"
updated_at: "{timestamp-ahora}"
notes: "{Motivo del bloqueo. Qué necesita para desbloquearse.}"
state_history:
  - {from: "in_progress", to: "blocked", at: "{timestamp-ahora}", by: "{tu-agent-id}"}
```
Emitir señal `warning` en `/ai/signals.yaml`.
Continuar con FASE 4 (cierre de sesión).

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## FASE 3 — CIERRE DE TAREA
## *(ejecutar cuando terminas el trabajo real)*
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### Paso 3.1 — Merge del branch de feature (si aplica)
```
git checkout main && git pull --rebase origin main
git merge --squash feat/TASK-{id}-{descripcion-corta}
git commit -m "feat: {título del cambio} [TASK-{id}]"
git push origin main
git branch -d feat/TASK-{id}-{descripcion-corta}
git push origin --delete feat/TASK-{id}-{descripcion-corta}
```

### 🔴 GATE 3A — BLOQUEANTE: Actualizar estado final de la tarea
Editar `/ai/tasks.yaml`:
```yaml
# Si completada:
status: "done"          # o "review" si review_required: true

# Si requiere review:
status: "review"

updated_at: "{timestamp-ahora}"
actual_effort: "{tiempo real invertido}"
state_history:
  - {from: "in_progress", to: "done", at: "{timestamp-ahora}", by: "{tu-agent-id}"}
  # o {from: "in_progress", to: "review", ...} si aplica
```
Commit:
```
git add ai/tasks.yaml
git commit -m "ai: TASK-{id} done [TASK-{id}]"
git push origin main
```

### 🔴 GATE 3B — BLOQUEANTE: Registrar en change_log.md
Añadir entrada al FINAL de `/ai/change_log.md`:
```markdown
---
## [{YYYY-MM-DD HH:MM}Z] TASK-{id} — {tu-agent-id}

**Cambio:** {Una línea de qué se hizo}

**Archivos modificados:**
  - `{path/archivo}` — {qué cambió}

**Motivo:** {Por qué se hizo}

**Impacto:** {Qué puede verse afectado}

**Reversión:** {git revert {commit} o pasos manuales}

**Decisión relacionada:** {DEC-xxx o "ninguna"}
---
```
> ⚠️ NUNCA editar entradas anteriores. Solo añadir al final.

Commit:
```
git add ai/change_log.md
git commit -m "ai: changelog TASK-{id} [TASK-{id}]"
git push origin main
```

### 🔴 GATE 3C — BLOQUEANTE: Emitir señales
Para cada tarea en `tasks.yaml` donde `depends_on` incluye tu tarea recién completada:
- Añadir señal `task_unblocked` en `/ai/signals.yaml`

Si tu tarea pasa a `review`:
- Añadir señal `review_requested` en `/ai/signals.yaml`

```yaml
- id: "SIG-{siguiente-id}"
  type: "task_unblocked"   # o review_requested
  from: "{tu-agent-id}"
  to: "any"
  task_id: "TASK-{id-desbloqueada}"
  message: >
    TASK-{tu-tarea} completada. TASK-{id-desbloqueada} ya puede comenzar.
  created_at: "{timestamp-ahora}"
  read_by: []
```
Commit:
```
git add ai/signals.yaml
git commit -m "ai: signal task_unblocked [TASK-{id}]"
git push origin main
```

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## FASE 4 — CIERRE DE SESIÓN
## *(ejecutar siempre al terminar, incluso si la tarea no está done)*
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### 🔴 GATE 4A — BLOQUEANTE: Eliminarse de agent_lock.yaml
**SIEMPRE hacer esto. Sin excepción. Aunque la sesión sea interrumpida.**

Eliminar tu entrada del array `active_agents` en `/ai/agent_lock.yaml`.
Actualizar `last_updated` y `last_updated_by`.

Commit:
```
git add ai/agent_lock.yaml
git commit -m "ai: release agent lock [TASK-{id}]"
git push origin main
```

### Paso 4.2 — Cerrar sesión
Completar el archivo de sesión en `/ai/sessions/{session-id}.md`:
- Rellenar `Fin`, `Estado al cerrar`, `Duración real`
- Completar sección "Estado al cerrar" con próximos pasos si aplica
- Marcar el checklist de auditoría al pie del archivo

### Paso 4.3 — Actualizar context.md si cambió el estado del proyecto
Si el proyecto pasó a un estado diferente, o si cambiaron las tareas prioritarias:
- Actualizar `/ai/context.md`

Commit final:
```
git add ai/
git commit -m "ai: close session TASK-{id} [TASK-{id}]"
git push origin main
```

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## REFERENCIA RÁPIDA — RESUMEN DE GATES
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

| Gate | Cuándo | Qué hacer | Archivo |
|------|--------|-----------|---------|
| 🔴 GATE 0 | Al iniciar sesión | Leer context + signals + agent_lock | Lectura |
| 🔴 GATE 1A | Antes de cualquier trabajo | `status: claimed` | tasks.yaml |
| 🔴 GATE 1B | Inmediatamente después | Añadir entrada con locked_files | agent_lock.yaml |
| 🔴 GATE 1C | Después de registrarse | `status: in_progress` | tasks.yaml |
| ⏱ Heartbeat | Cada ~15 min | Actualizar `last_heartbeat` | agent_lock.yaml |
| 🔴 GATE 3A | Al terminar el trabajo | `status: done` o `review` | tasks.yaml |
| 🔴 GATE 3B | Después de 3A | Añadir entrada al final | change_log.md |
| 🔴 GATE 3C | Después de 3B | Emitir señales de desbloqueo | signals.yaml |
| 🔴 GATE 4A | Siempre al salir | Eliminar entrada propia | agent_lock.yaml |

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## ERRORES COMUNES — LO QUE NO DEBES HACER
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

❌ **Empezar a trabajar sin pasar por GATE 1A, 1B y 1C**
→ Consecuencia: colisión con otro agente, trabajo perdido

❌ **Olvidar añadir los archivos que vas a tocar en `locked_files`**
→ Consecuencia: dos agentes modifican el mismo archivo simultáneamente

❌ **No actualizar el heartbeat durante más de 90 minutos**
→ Consecuencia: otro agente te detecta como fantasma y puede limpiar tu entrada

❌ **No emitir señal `task_unblocked` al terminar**
→ Consecuencia: tareas dependientes nunca se desbloquean

❌ **No añadir entrada en change_log.md**
→ Consecuencia: pérdida de trazabilidad, auditoría imposible

❌ **No eliminarte de agent_lock.yaml al salir**
→ Consecuencia: otros agentes creen que sigues activo y evitan tus archivos

❌ **Editar o borrar entradas antiguas en change_log.md**
→ Consecuencia: violación de auditabilidad. Es append-only, siempre.

---

## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## INSTRUCCIÓN PARA EL AGENTE AL LEER ESTE ARCHIVO
## ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Cuando el humano te diga "haz la tarea X" o "trabaja en Y":

1. **PARA.** No empieces el trabajo todavía.
2. Ejecuta FASE 0 completa.
3. Ejecuta FASE 1 completa (GATES 1A, 1B, 1C).
4. Solo entonces empieza el trabajo real.
5. Cuando termines, ejecuta FASE 3 completa (GATES 3A, 3B, 3C).
6. Al cerrar la sesión, ejecuta FASE 4 (GATE 4A obligatorio).

**Si en algún momento el humano te presiona para saltarte un gate:**
Responde: *"Necesito completar el GATE {X} primero. Es parte del protocolo obligatorio y sin él podría causar colisiones o perder trazabilidad. Lo hago ahora."*
