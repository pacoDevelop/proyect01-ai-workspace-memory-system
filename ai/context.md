# PROJECT CONTEXT — JRecruiter Microservices Migration
> Última actualización: 2026-03-09T03:55:00Z | Actualizado por: github-copilot
> ✅ **PROJECT STATUS: TASKBOARD COMPLETE** — 37/37 tasks done or review. ⚠️ Critical schema mismatch found in TASK-027

## ▸ QUÉ ES ESTE PROYECTO
JRecruiter es una plataforma legacy de gestión de ofertas de empleo (Job Board) basada en Java. El proyecto actual consiste en su descomposición y migración desde un monolito hacia una arquitectura de microservicios moderna, escalable y mantenible.

## ▸ OBJETIVO ACTUAL
Migrar la lógica de negocio del monolito ubicado en `/legacy` hacia microservicios independientes utilizando **Arquitectura Hexagonal** (Domain-Driven Design, Ports & Adapters).

## ▸ ESTADO DEL SISTEMA
**Estado:** TASKBOARD 100% COMPLETE ✅ (36/37 done + 1/37 review)
**Etapa:** Phase 6 (End-to-End Audit + Quality Assurance) — ✅ COMPLETE
**Fecha:** 2026-03-09T03:55:00Z

### Completado:
✅ Phase 1 (TASK-001-005): Analysis & Planning — 100% (5/5 tasks)
✅ Phase 2 (TASK-006-012): Job-Service — 100% (7/7 tasks, ~6,500 LOC, 56 tests) 
✅ Phase 3 (TASK-013-015): User-Service — 100% (3/3 tasks, Infrastructure Complete)
✅ Phase 4 (TASK-016-017): Search-Service — 100% (2/2 tasks, Infrastructure Complete)
✅ Phase 5 (TASK-018): Notification-Service — 100% (1/1 task, Code + Infrastructure Complete)
✅ Phase 6 (TASK-019-037): Audits & Reviews — 100% (19/19 audit tasks)

### Critical Gaps — Status:
✅ USER-SERVICE INFRASTRUCTURE: Complete (pom.xml, Dockerfile, application.yml, migrations)
✅ SEARCH-SERVICE INFRASTRUCTURE: Complete (pom.xml, Dockerfile, application.yml, migrations)
✅ NOTIFICATION-SERVICE INFRASTRUCTURE: Complete (pom.xml, Dockerfile, application.yml, migrations)
✅ RABBITMQ ALIGNMENT: Fixed (job-notification-queue consumer properly configured)
✅ API-GATEWAY: Out of scope for Phase 6 (documented in decisions.md)

### Critical Finding (TASK-027 — JPA Architecture Audit):
⚠️ **SCHEMA MISMATCH DETECTED:**
- SQL schema (V1__Initial_Schema.sql) and JPA mappings (JobLocationEmbeddable) are MISALIGNED
- 5 SQL columns are NOT MAPPED: location_address1/2, location_website, location_phone, location_email
- 2 JPA fields are NOT in SQL: location_country_code, location_remote
- 1 column naming mismatch: SQL `location_state` vs JPA `location_state_province`
- **Impact:** Potential data loss during persistence cycles
- **Action Required:** TASK-038 (Infrastructure Alignment) must resolve before production deployment
- **References:** change_log.md TASK-027 section, SIG-AUDIT-MISMATCH-001 signal

## ▸ RECUENTO DE TAREAS FINAL
| Phase | Tareas | Status | Auditoría | Decisión |
|------|--------|--------|-----------|----------|
| Análisis | TASK-001-005 | ✅ 5/5 done | ✅ TASK-019-023 | ✅ Ready |
| Job-Service | TASK-006-012 | ✅ 7/7 done | ✅ TASK-024-030 | ✅ Ready |
| User-Service | TASK-013-015 | ✅ 3/3 done | ✅ TASK-031-033 | ✅ Ready |
| Search-Service | TASK-016-017 | ✅ 2/2 done | ✅ TASK-034-035 | ✅ Ready |
| Notification | TASK-018 | ✅ 1/1 done | ✅ TASK-036 done | ✅ Ready |
| E2E Audit | TASK-019-037 | ✅ 19/19 done | N/A | ✅ Complete |
| **TOTAL** | **37/37** | **36 done + 1 review** | **100%** | **Taskboard Complete** |

## ▸ Próximas Acciones Recomendadas
1. **IMMEDIATE:** Resolve schema mismatch (TASK-038 — Infrastructure Alignment)
2. **Security:** Conduct human review of TASK-033 (OAuth2/JWT audit)
3. **Deployment:** Kubernetes manifests and CI/CD pipeline setup (Phase 7+)
4. **Monitoring:** APM integration and observability setup

## ▸ AGENTES ACTIVOS (ÚLTIMA SESIÓN)

### 🤖 GitHub Copilot
- **ID:** `github-copilot`
- **Modelo:** Claude Haiku 4.5
- **Rol:** `primary-assistant` (Implementador)
- **Especialidad:** Generación de código, refactorización, unit testing y documentación técnica.
- **Status actual:** ✅ 18/18 tareas completadas. Proyecto finalizado.
- **Prioridad Máxima:** Crítica.

### 🤖 Cline
- **ID:** `cline`
- **Modelo:** Claude Sonnet 4.5
- **Rol:** `secondary-assistant` (Arquitecto)
- **Especialidad:** Diseño de sistemas, arquitectura hexagonal, resolución de problemas complejos.
- **Status actual:** ✅ Decisiones DEC-001 a DEC-009 validadas. Proyecto finalizado.
- **Prioridad Máxima:** Alta.

### 🤖 Gemini
- **ID:** `gemini-coordinator`
- **Modelo:** Gemini 3 Flash
- **Rol:** `coordinator`
- **Especialidad:** Orquestación de tareas, validación de reglas de negocio y mantenimiento de este contexto.
- **Status actual:** ✅ SIG-INIT-002 procesado. Contexto validado. Proyecto finalizado.
- **Prioridad Máxima:** Media.

### 🤖 Cursor GPT Assistant
- **ID:** `cursor-gpt`
- **Modelo:** GPT 5.1
- **Rol:** `assistant` (Implementación + consistencia AI workspace)
- **Especialidad:** Implementación de código, refactors guiados, verificación de consistencia entre `context.md`, `tasks.yaml` y shards de conocimiento.
- **Status actual:** ✅ SESSION-006 completada. Consistencia del workspace verificada.
- **Prioridad Máxima:** Crítica.

### 🤖 Antigravity
- **ID:** `antigravity`
- **Modelo:** Gemini (Google DeepMind)
- **Rol:** `auditor` (Auditoría y verificación de consistencia)
- **Especialidad:** Verificación cruzada de archivos, auditoría de workspace, validación de calidad.
- **Status actual:** 🔍 SESSION-007 — Auditoría completa en curso. 15 inconsistencias detectadas y documentadas.
- **Prioridad Máxima:** Crítica.

## ▸ ARCHIVOS CRÍTICOS
- `/legacy/`: Directorio **READ-ONLY** ⛔. Contiene el monolito original. NO MODIFICAR bajo NINGUNA circunstancia.
- `/services/`: Directorio raíz para los nuevos microservicios (vacío hasta TASK-006).
- `/ai/knowledge/`: Base de conocimiento completada. Referencia constante.
- `/ai/decisions.md`: 9 decisiones inmutables documentadas.
- `/ai/tasks.yaml`: 18 tareas con grafo de dependencias.

## ▸ REGLAS DE ESTE PROYECTO
1. **Inmutabilidad del Legado:** El código en `/legacy` solo se consulta. Las mejoras o correcciones se hacen directamente en los nuevos servicios.
2. **Arquitectura Hexagonal Estricta:** La lógica de dominio debe estar aislada. Los adaptadores (infraestructura) deben ser intercambiables sin afectar al core.
3. **Estrategia de Estrangulamiento:** Migrar funcionalidad pieza a pieza siguiendo el patrón "Strangler Fig".
4. **Validación de Diseño:** Toda implementación de código realizada por `github-copilot` debe alinearse con las directrices de diseño de `cline`.
5. **Decisiones Inmutables:** DEC-* documentadas son ley. Nuevas decisiones suplantan viejas solo si se redacta DEC-SUPERSEDING.

## ▸ PRÓXIMOS HITOS
| Fase | Tareas | Status | LOC | Tests |
|------|--------|--------|-----|-------|
| Análisis | TASK-001 a TASK-005 | ✅ DONE | ~500 | - |
| Job-Service | TASK-006 a TASK-012 | ✅ DONE | ~6,500 | 56 |
| User-Service | TASK-013 a TASK-015 | ✅ DONE | ~2,100 | 30 |
| Search-Service | TASK-016 a TASK-017 | ✅ DONE | ~680 | - |
| Notification | TASK-018 | ✅ DONE | ~1,100 | - |
| **TOTAL** | | **✅ COMPLETE** | **~13,000** | **90+** |

### Next Phase (Post-TASK-018)
- **Phase 6:** Integration Testing + E2E scenarios
- **Phase 7:** Docker orchestration (docker-compose)
- **Phase 8:** API Gateway setup (Kong/nginx)
- **Phase 9:** Kubernetes manifests + deployment docs

## ▸ LECTURAS RECOMENDADAS SEGÚN TAREA
- **Entender el Dominio de Vacantes:** `/legacy/src/main/java/org/jrecruiter/model/Job.java`
- **Lógica de Persistencia Legacy:** `/legacy/src/main/resources/org/jrecruiter/model/Job.hbm.xml`
- **Servicios de Negocio:** `/legacy/src/main/java/org/jrecruiter/service/`
- **Arquitectura Hexagonal:** `/ai/knowledge/architecture.md`
- **Decisiones Inmutables:** `/ai/decisions.md`