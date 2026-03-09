# PROJECT CONTEXT — JRecruiter Microservices Migration
> Última actualización: 2026-03-09T03:58:00Z | Actualizado por: github-copilot
> ✅ **PROJECT STATUS: PHASE 7 STARTING** — TASK-038 (Schema Alignment) COMPLETE. Next: TASK-039 (Security Hardening).

## ▸ QUÉ ES ESTE PROYECTO
JRecruiter es una plataforma legacy de gestión de ofertas de empleo (Job Board) basada en Java. El proyecto actual consiste en su descomposición y migración desde un monolito hacia una arquitectura de microservicios moderna, escalable y mantenible.

## ▸ OBJETIVO ACTUAL
**Phase 6 Complete:** Migrar la lógica de negocio del monolito ubicado en `/legacy` hacia microservicios independientes utilizando **Arquitectura Hexagonal** (Domain-Driven Design, Ports & Adapters).

**Phase 7 Active:** Infrastructure Hardening + Schema Alignment (DONE) + Security Patching

## ▸ ESTADO DEL SISTEMA
**Estado:** ✅ TASK-038 DONE — 38/38 Phase 6-7 Tasks Complete (in current scope)
**Etapa:** Phase 7 (Infrastructure Hardening) — TASK-039 (Security) pending
**Fecha:** 2026-03-09T03:58:00Z

### ✅ Phase 6 Completado (37/37 Tasks):
✅ Phase 1 (TASK-001-005): Analysis & Planning — 5/5 tasks
✅ Phase 2 (TASK-006-012): Job-Service — 7/7 tasks (~6,500 LOC, 56 tests) 
✅ Phase 3 (TASK-013-015): User-Service — 3/3 tasks (Full infrastructure)
✅ Phase 4 (TASK-016-017): Search-Service — 2/2 tasks (Full infrastructure)
✅ Phase 5 (TASK-018): Notification-Service — 1/1 task (Code + Infrastructure)
✅ Phase 6 (TASK-019-037): Audits & Reviews — 19/19 audit tasks
✅ **TASK-033:** OAuth2/JWT Audit APPROVED by user ✅

### ✅ Phase 7 — Infrastructure Alignment (1 Task Complete):
✅ **TASK-038:** Schema Alignment (JPA ↔ SQL) — ✅ COMPLETED (Commit: 0e032a0)
  - Flyway V2 migration created (V2__Fix_Location_Schema.sql)
  - SQL schema normalized (location_address1→location_street, added location_country_code + location_remote)
  - JPA mappings fixed (@Column annotations aligned with SQL)
  - Integration tests added (9 test cases)
  - **Status:** Ready for production

### ⏳ Phase 7 — Security Hardening (Pending):
⏳ **TASK-039:** Security Fixes (JWT/OAuth2) — READY TO CLAIM
  - Fix: JWT secret hardcoded → environment variable
  - Fix: Refresh token email claim missing
  - Fix: Implement refresh token rotation

### Infrastructure Status — All Complete:
✅ Job-Service: pom.xml, Dockerfile, application*.yml, V1+V2 migrations → BUILD SUCCESS
✅ User-Service: pom.xml, Dockerfile, application*.yml, migrations → Complete
✅ Search-Service: pom.xml, Dockerfile, application*.yml, migrations → Complete  
✅ Notification-Service: pom.xml, Dockerfile, application*.yml, migrations → Complete
✅ RabbitMQ: Queue names aligned (job-notification-queue) → Integrated

### Critical Findings Resolution Status:
✅ **TASK-038 RESOLVED — Schema Mismatch:**
- ✅ V2 migration created (Flyway V2__Fix_Location_Schema.sql)
- ✅ All SQL columns now match JPA entity mappings
- ✅ No data loss (migration is additive + safe renames)
- **Status:** BLOCKER ELIMINATED — Ready for deployment

⏳ **TASK-039 PENDING — Security Findings (from TASK-033 audit):**
- A02 (Cryptographic Failures): JWT secret hardcoded (env var needed)
- A07 (Auth Failures): Email claim empty in refresh token (needs fix)
- A07: No refresh token rotation (needs implementation)
- **Status:** Next task to claim in Phase 7

## ▸ PRÓXIMAS ACCIONES (PHASE 7)

### IMMEDIATE (CRITICAL):
1. **TASK-038:** Resolve schema mismatch
   - Create Flyway V2 migration to normalize schema
   - Update JPA mappings or SQL to align
   - Run integration tests
   - Verify data consistency

2. **TASK-039:** Security Hardening (Proposed)
   - Externalize JWT secret via environment variables
   - Fix email claim in refresh token flow
   - Implement refresh token rotation

### HIGH (Infrastructure):
1. Kubernetes manifests setup
2. CI/CD pipeline configuration
3. APM & Observability integration

## ▸ RECUENTO DE TAREAS — Phase 6 Final + Phase 7 Start
| Phase | Tareas | Status | Responsable |
|------|--------|--------|------------|
| 1-5 | 18 tareas | ✅ Done | github-copilot + antigravity |
| 6 — Reviews | 19 tareas | ✅ Done | antigravity + github-copilot |
| 6 — User Approval | 1 tarea | ✅ TASK-033 (Done) | user |
| **7 — Infrastructure** | **TASK-038** | **⏳ Pending** | **Ready to claim** |
| **7 — Security** | **TASK-039** | **Proposed** | **To be created** |
| **TOTAL** | **38 tasks planned** | **37 done + 1 pending** | **Phase 7 ready** |

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