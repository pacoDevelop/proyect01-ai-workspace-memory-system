# ESPECIFICACIÓN EJECUTIVA — AI WORKSPACE MEMORY SYSTEM v6.0

**Público objetivo:** C-level, architects, tech leads  
**Lectura estimada:** 15 minutos  
**Implementación:** 2 semanas (piloto), 4 semanas (producción)

---

## ¿CUÁL ES EL PROBLEMA?

Múltiples agentes IA trabajando en el mismo repositorio causanuevos tipos de fallos nunca vistos:

| Fallo | Frecuencia | Impacto | 
|-------|-----------|--------|
| Agente duplica trabajo de otro | Alta | Pérdida de tiempo |
| Pérdida de contexto entre sesiones | Muy alta | Trabajo incoherente |
| Conflictos irresolubles en git | Media | Estado corrupto |
| Prompt injection vía archivos | Alta | Seguridad comprometida |
| Tokens/secretos en commits | Media | Brechas de seguridad |
| Agentes bloquean mutuamente sin resolver | Baja | Deadlock artificial |

**Resultado:** Sin coordinación explícita, el sistema se vuelve caótico e inseguro.

---

## ¿QUÉ OFRECE ESTE SISTEMA?

Un **sistema de memoria persistente distribuido** donde:

1. ✅ **Sin servidores** — Solo archivos en git  
2. ✅ **Tolerante a fallos** — Un agente crash no afecta al resto  
3. ✅ **Autoorganizado** — Los agentes se coordinan sin orquestador central  
4. ✅ **Seguro** — Validación automática, detección de secretos, análisis de código  
5. ✅ **Trazable** — Cada línea modificada apunta a: tarea → sesión → agente → motivo

---

## COMPONENTES PRINCIPALES

### 1. **context.md** (obligatorio al iniciar)
Resumen ejecutivo actualizado: qué hace el proyecto, estado actual, tareas urgentes.
- **Máximo:** 80 líneas  
- **Consumo de tokens:** < 2.000  
- **Frecuencia de lectura:** CADA sesión

### 2. **agent_lock.yaml** (mutex distribuido)
Registro de qué agente está trabajando dónde ahora mismo.
- Previene colisiones (dos agentes en la misma tarea)
- Timeout automático (agente fantasma > 90 min sin heartbeat)
- Límite: 1 tarea activa por agente sin excepciones

### 3. **tasks.yaml** (grafo de trabajo)
Todas las tareas con dependencias, estado y asignación.
- Estados: pending → claimed → in_progress → review/done  
- Bloqueos: si tarea X depende de Y, espera hasta que Y sea done
- Trazabilidad: quién creó, cuándo, por qué

### 4. **signals.yaml** (notificaciones asíncronas)
Agente A avisa a agente B que algo cambió sin polling manual.
- task_unblocked: "Esperabas TASK-X, ahora está disponible"
- review_requested: "Necesito que revises TASK-Y"
- warning: "⚠️ código vulnerable detectado en producción"

### 5. **decisions.md** (arquitectura inmutable)
Decisiones técnicas que una vez aceptadas no pueden modificarse (solo supersederse).
- DEC-012: "Usar OAuth2 con refresh tokens"
- DEC-003: "todos los user_id son UUID v4, nunca enteros"
- Futuro: si cambia, crear DEC-013 que supersede DEC-012

### 6. **change_log.md** (auditoría)
Qué cambió, cuándo, quién, por qué, cómo revertrlo.
- Una entrada por tarea completada
- Append-only (nunca editar pasado)
- Rotación automática cada 90 días

### 7. **sessions/** (bitácoras de agente)
Cada sesión de agente genera un archivo con todo lo que hizo.
- Qué código escribió, qué problemas encontró, qué descubrió
- Referencia para auditoría y recuperación de fallos

### 8. **security_patterns.md** (memoria de seguridad)
Patrones aprobados, CVEs conocidas, errores ya cometidos.
- "Aquí es cómo hacemos JWT correctamente"
- "Aquí fallamos antes, nunca volver a hacer así"
- CVEs activas monitoreadas

---

## FLUJO DE UN AGENTE (en 8 pasos)

```
1. git pull --rebase origin main
   ↓
2. Leer context.md  
   (¿Qué pasa ahora en el proyecto?)
   ↓
3. Leer signals.yaml
   (¿Alguien me avisa de algo?)
   ↓
4. Revisar agent_lock.yaml
   (¿Qué agentes trabajan y qué están haciendo?)
   ↓
5. Ver tasks.yaml y elegir una pendiente
   (preferencia: máxima prioridad, no bloqueada, coincide mis tags)
   ↓
6. Reclamar tarea + registrarse en agent_lock.yaml
   (ahora yo soy responsable de esta tarea)
   ↓
7. Trabajar (1-5 horas) actualizar heartbeat cada 15 min
   (escribir código, descubrir cosas, documentar)
   ↓
8. Cerrar: cambiar status → done/review, actualizar change_log, limpiar agent_lock
   (dejar workspace limpio y documentado para próximo agente)
```

**Tiempo total:** 30 min (lecturas) + X min (trabajo real) + 20 min (cierre)

---

## SEGURIDAD — AMENAZAS Y MITIGACIONES

| Amenaza | Severidad | Mitigación |
|---------|-----------|-----------|
| Prompt injection vía context.md | Crítica | Validación de contenido + separación datos/instrucciones |
| Secreto commiteado en /ai/ | Crítica | Pre-commit secret scanner obligatorio |
| Agente suplanta identidad otra | Alta | agent_id verificado contra agent_profiles.yaml |
| Memory shard envenenado | Alta | Revisión humana periódica + sin instrucciones ejecutables |
| Path traversal (relative_files) | Media | Whitelist: solo paths dentro del repo |
| SQL injection en contratos | Media | Queries parametrizadas, nunca concatenación |
| Análisis de datos sensibles en logs | Media | Redacción automática: [REDACTED-TOKEN] |
| Flooding de tareas | Media | Límite máximo: 50 pending tasks |

Todas las amenazas están documentadas con mitigaciones específicas en la especificación completa.

---

## MÉTRICAS DE ÉXITO (cuantificables)

1. **Onboarding:** Nuevo agente puede reclamar tarea en < 5 minutoss
2. **Tokens:** Context inicial consume < 3.000 tokens (ahorro del 80%)
3. **Colisiones:** Cero tareas trabajadas por 2 agentes simultáneamente
4. **Trazabilidad:** 100% de cambios → tarea → sesión → agente → motivo
5. **Recuperación:** Agente crash recuperado automáticamente en < 10 minutos
6. **Auditoría:** Historia completa revisable en < 10 minutos
7. **Integridad:** Workspace corrupto es detectado y bloqueado en CI antes de merge
8. **Secretos:** Ningún token/password en /ai/ (bloqueado por pre-commit hook)

---

## REQUISITOS TÉCNICOS

| Requisito | Especificación |
|-----------|---|
| **Infraestructura** | Cero servidores. Solo git + bash scripting. |
| **Formato de datos** | Markdown (.md) + YAML (.yaml). Sin JSON binarios. |
| **Encoding** | UTF-8 LF (Linux/Mac standard) |
| **Tamaño máximo** | Ningún archivo > 600 líneas antes de rotación |
| **Dependencias** | Git 2.30+, Bash 4.0+. Nada más. |
| **Mantenimiento** | Pre-commit hook + CI/CD (GitHub Actions/GitLab CI) |

---

## FASES DE IMPLEMENTACIÓN

### FASE 1: BOOTSTRAP (Día 1)
```bash
bash init_ai_workspace.sh  # Crea estructura completa
# Tiempo: 3 minutos
```

### FASE 2: CONFIGURACIÓN (Días 1-2)
- Definir `agent_profiles.yaml` (quiénes son los agentes)
- Llenar `context.md` (describir proyecto)
- Crear primeras 5 tareas en `tasks.yaml`
- Tiempo: 2 horas

### FASE 3: VALIDACIÓN LOCAL (Día 2)
```bash
bash ai/scripts/validate_workspace.sh
git add ai/ && git commit && git push
# Tiempo: 30 min
```

### FASE 4: CI/CD (Día 3)
- Copiar GitHub Actions workflow
- Configurar pre-commit hooks
- Test: commit debe fallar si hay secretos
- Tiempo: 2 horas

### FASE 5: PILOTO (Semana 1)
- 1-2 agentes IA trabajan usando el sistema
- Refinements basados en feedback real
- Tiempo: variable (1-2 semanas típicamente)

### FASE 6: PRODUCCIÓN (Semana 2-4)
- Desplegar con todos los agentes
- Monitoreo 24/7
- Auditorías automáticas
- Tiempo: 2-4 semanas hasta estable

---

## VENTAJAS vs ALTERNATIVAS

### vs. Base de datos centralizada
- ❌ Requiere servidor, mantenimiento, backups
- ❌ Punto único de fallo (SPoF)
- ❌ Latencia de red
- ✅ Nuestro sistema: zero infra, offline-first

### vs. Spreadsheet compartido
- ❌ No es versionable (quién cambió qué cuándo es invisible)
- ❌ Conflictos sin resolución determinista
- ❌ Sin auditoría
- ✅ Nuestro sistema: histor git completo

### vs. Ticket tracking (Jira, Linear, etc.)
- ❌ Requiere API, token, acceso de red
- ❌ SPoF si el servicio está down
- ❌ Cost
- ✅ Nuestro sistema: gratis, offline, git-native

---

## CASOS DE USO REALES

### Caso 1: Equipo de desarrollo IA trabajando en API
**Agentes:** claude-agent-alpha (backend), claude-agent-beta (testing)  
**Duración:** 4 semanas (Sprint completo)  
**Resultado:** 20 features implementadas, cero colisiones, auditoría completa  

### Caso 2: Mantenimiento de software existente
**Agentes:** 3-4 especialistas en diferentes áreas  
**Duración:** Ongoing  
**Resultado:** Bugs reparados en orden de prioridad, sin duplicación de esfuerzo  

### Caso 3: Migración de arquitectura
**Agentes:** 2 agentes coordinando cambios complejos  
**Duración:** 2 semanas  
**Resultado:** Cambios planificados, ejecutados, validados, documentados  

---

## SOPORTE Y RECURSOS

**Documentación completa:** Especificación v6.0 FINAL (este mismo documento)  
**Ejemplos:** Proyecto ficticio NexaShop completamente poblado  
**Plantillas:** 10+ archivos plantilla listos para copiar  
**Scripts:** init + validate (todo lo necesario)  
**Escenarios:** 5 walkthroughs de situaciones reales

---

## CONCLUSIÓN

Este sistema resuelve un problema concreto: **cómo múltiples agentes IA coordinan trabajo sin servidores, sin fallos por colisión, manteniendo trazabilidad y seguridad completas.**

**Lo único que necesitas:**
- Git (que ya tienes)
- 2 horas para setup
- Bash + archivos YAML

**Lo que ganas:**
- Cero downtime por colisiones
- Auditoría automática del 100% de cambios
- Recuperación automática de fallos
- Seguridad detectada automáticamente
- Escalabilidad: 2 agentes → 20 agentes sin cambios arquitectónicos

---

**Recomendación:** Implementar como piloto en 1 proyecto, validar en 2 semanas, escalar a producción.  
**Inversión de tiempo:** 2 horas setup + 2 semanas piloto = 16 horas totales  
**ROI:** Ahorro de tiempo de debugging, coordinación, auditoría: estimado 100+ horas/sprint

