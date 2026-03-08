# SESSION: {YYYYMMDD-HHMMSS-agent-name}

| Campo | Valor |
|-------|-------|
| **Agente** | {agent-identifier} |
| **Tarea principal** | TASK-{id} |
| **Inicio** | {YYYY-MM-DDTHH:MM:SSZ} |
| **Fin** | {YYYY-MM-DDTHH:MM:SSZ} \| `EN PROGRESO` |
| **Estado al cerrar** | COMPLETADO \| INTERRUMPIDO \| BLOQUEADO \| PARCIAL |
| **Duración real** | {Xh Ym} |

---

## Objetivo de la sesión
{Qué se buscaba lograr al comenzar. Una o dos líneas máximo.}

---

## Contexto inicial
{Qué estado tenía el proyecto al empezar. Lo más relevante para entender el trabajo.}

---

## Trabajo realizado

### Bloque 1: {Descripción clara}
{Qué se hizo, por qué, cómo. Describe el proceso y cualquier decisión tomada.}

### Bloque 2: {Descripción clara}
{Descripción del trabajo realizado.}

---

## Archivos modificados

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `path/file1` | crear \| editar \| eliminar | {Qué cambió} |
| `path/file2` | editar | {Qué cambió} |

---

## Problemas encontrados

### Problema: {Descripción corta}
**Síntoma:** {Qué se observó}
**Causa raíz:** {Qué lo causó}
**Solución:** {Cómo se resolvió}
**Estado:** resuelto \| workaround \| pendiente

---

## Decisiones tomadas durante la sesión

| Decisión | Motivo | Referencia |
|----------|--------|-----------|
| {Qué se decidió} | {Por qué} | DEC-{id} si se creó |

---

## Conocimiento nuevo descubierto

| Shard actualizado | Qué se añadió |
|-------------------|---------------|
| `/ai/memory/{shard}.md` | {Descripción del conocimiento nuevo} |

---

## Tareas generadas

| TASK | Título | Prioridad | Motivo |
|------|--------|-----------|--------|
| TASK-{id} | {Título} | {priority} | {Por qué se creó como resultado de esta sesión} |

---

## Estado al cerrar

**Resultado:** {COMPLETADO / INTERRUMPIDO / BLOQUEADO / PARCIAL}

**Motivo si no fue COMPLETADO:**
{Explicación clara de por qué no se completó la tarea principal.}

**Próximo agente debe:**
{Instrucciones específicas para continuar si aplica. Qué está quedando pendiente.}

**Advertencias y notas importantes:**
{Algo que el próximo agente DEBE saber antes de continuar.}

---

## Checkpoint para auditoría
- [ ] Context.md actualizado si cambió estado del proyecto
- [ ] Change_log.md tiene entrada para esta sesión
- [ ] Agent_lock.yaml fue limpiado (agente removido)
- [ ] Señales enviadas a otros agentes si aplica
- [ ] No hay secretos o datos sensibles en este archivo
