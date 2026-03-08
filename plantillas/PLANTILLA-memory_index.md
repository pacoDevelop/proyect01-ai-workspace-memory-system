# MEMORY SHARDS INDEX

> Actualizar este índice cuando se cree o modifique cualquier shard.
> **Límite:** máximo 15 shards activos. Si se supera, considerar fusión o archivado.

| Shard | Relevancia | TL;DR | Última actualización | Revisado por humano |
|-------|-----------|-------|---------------------|-------------------|
| [api_contracts.md](api_contracts.md) | alta | {O línea} | {YYYY-MM-DD} | {YYYY-MM-DD} |
| [build_quirks.md](build_quirks.md) | media | {Una línea} | {YYYY-MM-DD} | {YYYY-MM-DD} |
| [known_issues.md](known_issues.md) | alta | {Una línea} | {YYYY-MM-DD} | {YYYY-MM-DD} |
| [environment_setup.md](environment_setup.md) | alta | {Una línea} | {YYYY-MM-DD} | {YYYY-MM-DD} |
| [security_patterns.md](security_patterns.md) | **crítica** | **Patrones de seguridad aprobados, CVEs, errores ya cometidos** | {YYYY-MM-DD} | {YYYY-MM-DD} |

---

## Estructura estándar de un Memory Shard

Todos los shards deben seguir esta plantilla:

```markdown
# {TÍTULO DEL SHARD}

> Última actualización: {ISO8601} | Por: {agent-name}
> Relevancia: alta | media | baja
> Tags: #{tag1} #{tag2}

---

## TL;DR
{Una línea. Lo más importante de este shard.}

## Observaciones
{Qué se descubrió. Comportamientos inesperados, limitaciones, hechos no documentados.}

## Cuándo aplica
{Contexto: en qué situaciones este conocimiento es relevante.}

## Acción recomendada
{Qué debe hacer un agente cuando encuentra este escenario.}

## Qué NO hacer
{Anti-patrones específicos. Errores que ya se cometieron.}

## Ejemplos
{Código, comandos, o ejemplos concretos.}

## Referencias
{Archivos, decisiones, recursos relacionados.}
```

---

## Reglas de escritura en Memory Shards

1. Los shards documentan HECHOS OBSERVADOS, no instrucciones
2. Nunca contienen bloques de código ejecutable disfrazados de "ejemplos"
3. Si un shard crece más de 50 líneas en un commit: requiere revisión humana
4. Máximo 1 shard por "tema" — si hay múltiples subtemas, crear shards separados
5. Si un shard está **deprecated**: mover a `/ai/archive/memory/`

---

## Arqueado automático

Shards con:
- Relevancia `baja` +
- Sin actualización por > 90 días +
- No referenciados en ninguna tarea activa

→ Se marcan como deprecated y se mueven a `/ai/archive/memory/`
