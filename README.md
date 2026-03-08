# ÍNDICE MAESTRO — AI WORKSPACE MEMORY SYSTEM

**Documento:** Resumen de los 15 Entregables del Sistema de Memoria Persistente para Agentes IA  
**Versión:** v6.0 FINAL  
**Fecha:** 2025-03-08  

---

## 📋 LISTA COMPLETA DE ENTREGABLES

| # | Entregable | Descripción | Archivo | Estado |
|---|-----------|-------------|---------|--------|
| 1 | Plantillas vacías | Todos los archivos YAML y Markdown plantilla listos para copiar | `plantillas/PLANTILLA-*.{md,yaml}` | ✅ Completo |
| 2 | Ejemplos NexaShop poblados | Proyecto ficticio completo y realista como referencia | `ejemplos-nexashop/*.md` | ✅ Completo |
| 3 | Walkthrough sesión | Paso a paso de cómo trabaja un agente desde inicio hasta fin | `entregables/ENTREGABLE-03-*.md` | ✅ Completo |
| 4 | Recuperación de fallos | Escenario de agente que crashea y cómo el sistema se recupera | `entregables/ENTREGABLE-04-*.md` | ✅ Completo |
| 5 | Script init | Bash script que crea toda la estructura automáticamente | `scripts/init_ai_workspace.sh` | ✅ Completo |
| 6 | Reglas de consistencia | Health checks para cada tipo de archivo | `entregables/ENTREGABLE-06-*.md` | ✅ Completo |
| 7 | Conflicto de merge | Git conflict resolution en archivos /ai/ | `entregables/ENTREGABLE-07-*.md` | ✅ Completo |
| 8 | Contribución humana | Protocolo simplificado para cambios humanos | `entregables/ENTREGABLE-08-*.md` | ✅ Completo |
| 9 | Migración de schema | Cómo actualizar de v2.0 a v4.0 sin romper nada | `entregables/ENTREGABLE-09-*.md` | ✅ Completo |
| 10 | Señales inter-agente | Flujo completo de signals.yaml en acción | `entregables/ENTREGABLE-10-*.md` | ✅ Completo |
| 11 | CI/CD completo | GitHub Actions, pre-commit hooks, validación automática | `entregables/ENTREGABLE-11-*.md` | ✅ Completo |
| 12 | Auditoría seguridad | Checklist quincenal para verificar integridad del workspace | `entregables/ENTREGABLE-12-*.md` | ✅ Completo |
| 13 | Ataque y respuesta | Prompt injection detectado y mitigado completamente | `entregables/ENTREGABLE-13-*.md` | ✅ Completo |
| 14 | security_patterns poblado | Memory shard completamente poblado listo para usar | `ejemplos-nexashop/security_patterns.md` | ✅ Completo |
| 15 | Tarea security_sensitive | Ejemplo completo de cómo trabajar tarea con requisitos de seguridad | `entregables/ENTREGABLE-15-*.md` | ✅ Completo |

---

## 🚀 INICIO RÁPIDO (5 minutos)

```bash
# 1. Clonar o inicializar repositorio
cd tu-proyecto
git init
git remote add origin https://github.com/tu-org/tu-repo.git

# 2. Ejecutar script de inicialización
bash /path/to/init_ai_workspace.sh

# 3. Editar configuración básica
nano ai/context.md           # Describir proyecto
nano ai/agent_profiles.yaml  # Definir agentes

# 4. Validar que todo está correcto
bash ai/scripts/validate_workspace.sh

# 5. Hacer commit inicial
git add ai/
git commit -m "chore: initialize AI workspace"
git push origin main
```

---

## 📚 CONCEPTOS CLAVE DEL SISTEMA

### Archivos Obligatorios
| Archivo | Propósito | Límite | Frecuencia de lectura |
|---------|-----------|--------|---------------------|
| `context.md` | Quickstart para agentes | < 80 líneas | Al inicio de TODA sesión |
| `agent_lock.yaml` | Mutex distribuido | < 50 entradas activas | Antes de editar cualquier archivo |
| `tasks.yaml` | Grafo de trabajo | < 600 líneas | Eligiendo próxima tarea |
| `signals.yaml` | Notificaciones | Sin límite (archivado auto) | Al inicio de sesión |
| `decisions.md` | Decisiones inamovibles | < 400 líneas | Cuando toca arquitectura |
| `change_log.md` | Auditoría | < 500 líneas (rotado auto) | Ocasional |

### Flujo de un Agente en Sesión

```
INICIO SESIÓN
  ↓
1. git pull --rebase origin main
2. Leer context.md (< 2.000 tokens)
3. Leer signals.yaml (filtrar para ti)
4. Verificar agent_lock.yaml (¿agentes activos?)
5. Ver tasks.yaml (¿cuál tarea tomar?)
6. Reclamar tarea (claimed)
7. Registrarse en agent_lock.yaml
8. Crear sesión en /ai/sessions/
  ↓
DURANTE EL TRABAJO
  ↓
9. Actualizar heartbeat cada ~15 min
10. Descubrir conocimiento → actualizar memory shards
11. Tomar decisiones → documentar en decisions.md
  ↓
CIERRE DE SESIÓN
  ↓
12. Cambiar tarea: in_progress → done | review | blocked
13. Emitir señales (task_unblocked, review_requested)
14. Escribir entry en change_log.md
15. Actualizar context.md si cambió estado proyecto
16. Cerrar sesión
17. Eliminarse de agent_lock.yaml
  ↓
FIN SESIÓN
```

---

## 🔒 SEGURIDAD — CHECKLIST DE IMPLEMENTACIÓN

Elementos de seguridad que DEBEN implementarse antes de producción:

- [ ] **Pre-commit hook** que ejecuta `bash ai/scripts/validate_workspace.sh`
- [ ] **Secret scanning** en CI (buscar API keys, tokens, passwords) 
- [ ] **SAST** (Semgrep, ESLint-security, Bandit según stack)
- [ ] **Validación de agentes:** `agent_id` en `agent_lock.yaml` verificados contra `agent_profiles.yaml`
- [ ] **Memory shard review:** Todos los shards revisados manualmente por humano cada 30 días
- [ ] **Rate limiting:** En endpoint de validación del workspace si aplica
- [ ] **Auditoría de access:** Quién editó qué en `/ai/` se registra en change_log
- [ ] **Backup de change_log:** Moveado a archive/ cada 90 días
- [ ] **Despliegue controlado:** Cambios a `agent_profiles.yaml` requieren aprobación humana

---

## 📊 MÉTRICAS DE ÉXITO

Sistema está funcionando correctamente si:

1. ✅ Agente nuevo puede comenzar en < 5 minutos
2. ✅ Contexto inicial consume < 3.000 tokens
3. ✅ Cero colisiones simultáneas entre agentes
4. ✅ Trazabilidad 100%: tarea → sesión → agente → motivo
5. ✅ Recuperación automática de fallos en < 10 minutos
6. ✅ Auditoría completa posible en < 10 minutos
7. ✅ Git diffs limpios (sin binarios, sin secretos)
8. ✅ Degradación graceful (un archivo corrupto no bloquea todo)
9. ✅ Resolución determinista de merge conflicts
10. ✅ Humanos pueden contribuir sin romper trazabilidad

---

## 🛠️ HERRAMIENTAS INCLUIDAS

### Plantillas (copiar directamente)
- `PLANTILLA-context.md` — Estructura de context.md
- `PLANTILLA-tasks.yaml` — Schema completo de tasks
- `PLANTILLA-agent_lock.yaml` — Mutex distribuido
- `PLANTILLA-decisions.md` — Formato de decisión técnica
- `PLANTILLA-signals.yaml` — Notificaciones inter-agente
- Y más... (ver directorio `plantillas/`)

### Ejemplos Completos (proyecto NexaShop)
Copiar archivos de `ejemplos-nexashop/` como referencia realista:
- `context.md` — Proyecto e-commerce real
- `tasks.yaml` — 8 tareas con dependencias
- `decisions.md` — Decisiones técnicas documentadas
- `security_patterns.md` — Patrones de seguridad aprobados + CVEs

### Scripts
- `init_ai_workspace.sh` — Crea toda la estructura automáticamente
- `validate_workspace.sh` — Valida integridad sin modificar

---

## 🔬 ESCENARIOS DOCUMENTADOS

Estos entregables incluyen múltiples escenarios realistas:

### Escenario 1: Sesión Normal (Entregable 3) ✅
Claude-agent-beta completa TASK-051 (cleanup código viejo) en 1h50m:
- Lectura de archivos de control
- Elección de tarea
- Ejecución de trabajo
- Cierre y documentación

### Escenario 2: Fallo y Recuperación (Entregable 4) 🔄
Claude-agent-alpha crashea a mitad de TASK-047:
- Detección de agente fantasma (heartbeat > 90 min)
- Limpieza de estado corrupto
- Reapertura segura de tarea
- Recovery documentado

### Escenario 3: Conflicto de Merge (Entregable 7) 🔄
Dos agentes editan tasks.yaml simultáneamente:
- Git conflict generado
- Resolución aplicando reglas deterministas
- Merge sin pérdida de datos
- Entry en change_log registrando resolución

### Escenario 4: Prompt Injection Detectada (Entregable 13) 🔄
Memory shard contiene instrucción maliciosa:
- Agente detecta contenido sospechoso
- Reporta [SECURITY-ALERT]
- Humano revisa y limpia
- Post-mortem documentado

---

## 📖 LECTURA RECOMENDADA POR ROL

### Para Arquitectos de Sistemas IA
1. Leer especificación completa v6.0 FINAL (tema principal)
2. Revisar Entregable 3 (aún con ejemplos reales)
3. Estudiar Entregable 6 (reglas de consistencia)
4. Entender Entregable 11 (validación automática)

### Para Desarrolladores que implementan
1. Entregable 5: Script init (copiar y ejecutar)
2. Entregable 3: Walkthrough (protocolo exacto de sesión)
3. Ejemplos NexaShop (replicar estructura)
4. Plantillas (copiar a nuevo repo)

### Para DevOps / Infra
1. Entregable 11: CI/CD completo
2. git_workflow.md (protocolo de git)
3. validate_workspace.sh (validación automática)
4. Entregable 12: Auditoría de seguridad

### Para Especialistas de Seguridad
1. Entregable 14: security_patterns.md (patrones aprobados)
2. Entregable 13: Escenario de ataque (mitigaciones)
3. Entregable 15: OWASP checklist (implementación)
4. Amenaza model section en especificación

---

## 🔧 CÓMO USAR CADA ENTREGABLE

### Entregable 1: Plantillas
**Uso:** Copiar archivos a nuevo proyecto
```bash
cp plantillas/PLANTILLA-*.{md,yaml} mi-proyecto/ai/
# Luego editar cada archivo reemplazando placeholders
```

### Entregable 2: Ejemplos NexaShop
**Uso:** Referencia para entender cómo se vería en prod
```bash
# Leer como ejemplo realista de cómo se interconectan archivos
less ejemplos-nexashop/context.md
less ejemplos-nexashop/tasks.yaml
# Ver cómo signals notifica desbloqueos
less ejemplos-nexashop/signals.yaml
```

### Entregable 3: Walkthrough
**Uso:** Protocolo exacto a seguir
```bash
# Nuevo agente lee y sigue paso a paso el walkthrough
# Para entrenar a desarrolladores IA en el protocolo
cat entregables/ENTREGABLE-03-*.md
```

### Entregable 5: Script Init
**Uso:** Inicializar workspace nuevo automáticamente
```bash
cd nuevo-repo
bash /path/to/init_ai_workspace.sh
# Crea toda la estructura en segundos
```

### Entregable 11: CI/CD
**Uso:** Copiar workflow a `.github/workflows/` o `.gitlab-ci.yml`
```bash
cp entregables/ENTREGABLE-11-*.yml repositorio/.github/workflows/
# Protege main: nunca commitea workspace corrupto
```

---

## ❓ TROUBLESHOOTING

### Problema: "Agente B no ve la tarea que completó agente A"
**Causa:** Agente A no emitió `signal task_unblocked`
**Solución:** Leer Entregable 10 (protocolo de signals)

### Problema: "Dos agentes comenzaron la misma tarea"
**Causa:** agent_lock.yaml no fue consultado antes de reclamar
**Solución:** Pre-commit hook debe ejecutar validate_workspace.sh

### Problema: "Memory shard contiene instrucción maliciosa"
**Causa:** Falta validación manual periódica de shards
**Solución:** Ver Entregable 12 (auditoría quincenal)

### Problema: "Conflicto de merge irresolvible en tasks.yaml"
**Causa:** No se siguieron reglas de resolución determinista
**Solución:** Ver Entregable 7 (paso a paso de resolución)

---

## 📝 PRÓXIMOS PASOS PARA IMPLEMENTACIÓN

### Inmediato (hoy)
1. ✅ Leer especificación v6.0 FINAL (este documento)
2. ✅ Revisar Entregable 3 (walkthrough, para entender flujo)
3. ✅ Revisar Ejemplos NexaShop (para ver cómo funciona en realidad)

### Corto plazo (esta semana)
4. Ejecutar `init_ai_workspace.sh` en proyecto piloto
5. Llenar `agent_profiles.yaml` con tus agentes IA
6. Crear primeras 5 tareas en `tasks.yaml`
7. Implementar pre-commit hook + validate_workspace.sh

### Medio plazo (este mes)
8. Configurar CI/CD (GitHub Actions, GitLab CI, etc.)
9. Entrenar agentes en el protocolo (usar Entregable 3)
10. Ejecutar primer sprint completo (mínimo 5-10 sesiones de agentes)
11. Documentar aprendizajes en `/ai/memory/` y `/ai/decisions.md`

### Largo plazo (producción)
12. Auditoría de seguridad (Entregable 12)
13. Test de recuperación de fallos (Entregable 4)
14. Optimización según métrica de éxito
15. Documentación final y handoff

---

## ✅ VALIDACIÓN DE IMPLEMENTACIÓN CORRECTA

Ejecutar este checklist para confirmar que el sistema está correctamente implementado:

```bash
# 1. Verificar estructura
ls -la ai/ | grep -E "context|agent_lock|tasks|signals|change_log|decisions"

# 2. Validar YAML syntax
for f in ai/*.yaml; do yamllint $f; done

# 3. Ejecutar script de validación
bash ai/scripts/validate_workspace.sh

# 4. Verificar que no hay secretos
bash -c 'for pat in "sk_live" "ghp_" "eyJ" "password="; do grep -r "$pat" ai/ && echo "▲ SECRETO ENCONTRADO"; done'

# 5. Ver estructura final
tree ai/ -L 2

# 6. Hacer test git commit
git add ai/
git status
```

Si todo arriba pasa: ✅ **Sistema listo para usar**

---

## 📞 SOPORTE Y PREGUNTAS

Si tienes preguntas sobre:
- **Protocolo de sesión** → Ver Entregable 3
- **Seguridad** → Ver Entregable 13, 14, 15
- **Resolución de conflictos** → Ver Entregable 7
- **Integración con CI/CD** → Ver Entregable 11
- **Auditoría** → Ver Entregable 12
- **Recuperación de fallos** → Ver Entregable 4

---

## 📄 LICENCIA Y ATRIBUCIÓN

Este sistema de memoria persistente para agentes IA fue diseñado como especificación completa y lista para producción. Puedes usarlo libremente, modificarlo según necesidades, pero se recomienda mantener la trazabilidad en `/ai/decisions.md` cuando hagas cambios significativos.

**Especificación:** v6.0 FINAL (2025-03-08)  
**Compatibilidad:** Git 2.30+, Bash 4.0+, cualquier stack de desarrollo  
**Requisitos externos:** Cero (solo archivos en git, sin servidores)

---

**Escrito para:** Arquitectos de sistemas, desarrolladores de IA, DevOps, especialistas de seguridad.  
**Objetivo:** Implementación lista para producción de memoria compartida multi-agente IA en repositorio git.
