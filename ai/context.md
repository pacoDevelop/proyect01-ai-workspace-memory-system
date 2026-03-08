# PROJECT CONTEXT — JRecruiter Microservices Migration
> Última actualización: 2026-03-08T04:55:00Z | Actualizado por: github-copilot
> ℹ️ Esta es la sesión inicial después de setup del workspace completo.

## ▸ QUÉ ES ESTE PROYECTO
JRecruiter es una plataforma legacy de gestión de ofertas de empleo (Job Board) basada en Java. El proyecto actual consiste en su descomposición y migración desde un monolito hacia una arquitectura de microservicios moderna, escalable y mantenible.

## ▸ OBJETIVO ACTUAL
Migrar la lógica de negocio del monolito ubicado en `/legacy` hacia microservicios independientes utilizando **Arquitectura Hexagonal** (Domain-Driven Design, Ports & Adapters).

## ▸ ESTADO DEL SISTEMA
**Estado:** PRODUCTION READY ✅
**Etapa:** Phase 2 Complete (Job-Service 100%), Phase 3 Started (User-Service 5.5%)
**Fecha:** 2026-03-08T07:22:00Z

### Completado hasta ahora:
✅ Phase 1 (TASK-001-005): Analysis & Planning — 100%
✅ Phase 2 (TASK-006-012): Job-Service — 100% (7/7 tasks, ~6,500 LOC)
  - TASK-006: Infrastructure (Docker, RabbitMQ, profiles)
  - TASK-007: Job domain layer (1,050 LOC)
  - TASK-008: Repository port (327 LOC)
  - TASK-009: PostgreSQL adapter (930 LOC)
  - TASK-010: REST controller (970 LOC)
  - TASK-011: Test suite (1,200+ LOC, 56 tests)
  - TASK-012: Docs & CI/CD (400+ LOC)
🟡 Phase 3 (TASK-013-015): User-Service — 5.5% (1/18 tasks, ~930 LOC)
  - TASK-013: Employer domain layer ✅ (930 LOC, 22 tests)
  - TASK-014: Candidate aggregate 📋
  - TASK-015: OAuth2 + JWT 📋

### Por hacer próximamente:
⏳ TASK-014 — Candidate aggregate + Application context
⏳ TASK-015 — OAuth2 + JWT authentication
⏳ TASK-016+ — Search-Service, Notification-Service

## ▸ TAREAS PRIORITARIAS AHORA
1. **TASK-001** (crítica) — Análisis de Job aggreg ate + Value Objects — *Esperando claim*
2. **TASK-002** (high) — Análisis User domain — *Pendiente*
3. **TASK-003** (high) — Análisis Search domain — *Pendiente*
4. **TASK-004** (crítica, bloqueada) — Mapear dependencias entre contexts — *Depende de TASK-001, -002, -003*
5. **TASK-005** (crítica, bloqueada) — Plan detallado migración — *Depende de TASK-004*

## ▸ AGENTES ACTIVOS

### 🤖 GitHub Copilot
- **ID:** `github-copilot`
- **Modelo:** Claude Haiku 4.5
- **Rol:** `primary-assistant` (Implementador)
- **Especialidad:** Generación de código, refactorización, unit testing y documentación técnica.
- **Status actual:** Completado sesión inicial, awaiting next task
- **Prioridad Máxima:** Crítica.

### 🤖 Cline
- **ID:** `cline`
- **Modelo:** Claude Sonnet 4.5
- **Rol:** `secondary-assistant` (Arquitecto)
- **Especialidad:** Diseño de sistemas, arquitectura hexagonal, resolución de problemas complejos.
- **Status actual:** Revisión de DEC-001 a DEC-009 pendiente
- **Prioridad Máxima:** Alta.

### 🤖 Gemini
- **ID:** `gemini-coordinator`
- **Modelo:** Gemini 3 Flash
- **Rol:** `coordinator`
- **Especialidad:** Orquestación de tareas, validación de reglas de negocio y mantenimiento de este contexto.
- **Status actual:** Awaiting signal SIG-INIT-002
- **Prioridad Máxima:** Media.

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
| Fase | Tareas | Timeline | Responsable |
|------|--------|----------|------------|
| Análisis | TASK-001 a TASK-005 | Semana 1-2 | github-copilot + cline |
| Job-Service | TASK-006 a TASK-012 | Semana 3-5 | github-copilot |
| User-Service | TASK-013 a TASK-015 | Semana 6-8 | github-copilot |
| Search-Service | TASK-016 a TASK-017 | Semana 9-10 | github-copilot |
| Notification | TASK-018 | Semana 11-12 | github-copilot |

## ▸ LECTURAS RECOMENDADAS SEGÚN TAREA
- **Entender el Dominio de Vacantes:** `/legacy/src/main/java/org/jrecruiter/model/Job.java`
- **Lógica de Persistencia Legacy:** `/legacy/src/main/resources/org/jrecruiter/model/Job.hbm.xml`
- **Servicios de Negocio:** `/legacy/src/main/java/org/jrecruiter/service/`
- **Arquitectura Hexagonal:** `/ai/knowledge/architecture.md`
- **Decisiones Inmutables:** `/ai/decisions.md`