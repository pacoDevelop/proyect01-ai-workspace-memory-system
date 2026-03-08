# ARCHITECTURE DECISIONS — JRecruiter Migration

> Record of technical decisions. Accepted decisions are immutable.
> New decisions supersede old ones if requirements change.
> Last updated: 2026-03-08T04:55:00Z | Updated by: github-copilot

---

## DEC-001: Usar Arquitectura Hexagonal para Microservicios

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
JRecruiter necesita descomponerse de un monolito a microservicios. Necesitamos arquitectura que:
- Aïsle lógica de dominio de frameworks
- Permita testing sin BD o HTTP
- Facilite cambios en adaptadores (PostgreSQL → MongoDB, etc.)

### Decision
Adoptar **Ports & Adapters** (Hexagonal Architecture):
- **Domain Core:** Pure Java, sin Spring, sin Hibernate
- **Ports:** Contratos en forma de interfaces (repositories, services)
- **Adapters:** Implementaciones concretas (JPA, REST, RabbitMQ)

### Rationale
- ✅ Domain logic es 100% testeable sin mocks complejos
- ✅ Cambiar de PostgreSQL a MongoDB solo requiere nuevo adapter
- ✅ Domain no depende de versiones de Spring
- ✅ Arquitectura clara para nuevos developers

### Consequences
- Más archivos/carpetas (domain/, application/, infrastructure/)
- Necesita disciplina del equipo para no quebrar separación
- Requiere DTO mapping layers (overhead inicial)

---

## DEC-002: Java 21 + Spring Boot 3.4+ (sin soporte para versiones antiguas)

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Proyecto nuevo, no necesitamos compatibilidad backwards. Java 8 es EOL. Queremos:
- Últimas features de lenguaje
- Security patches modernos
- Soporte a largo plazo (LTS)

### Decision
- **Java:** Mínimo 21 LTS (no 8, 11, 17)
- **Spring Boot:** 3.4+ (no 2.x)
- **PostgreSQL:** 15+ (no MySQL 5.7)
- **Hibernat e:** Incluido en Spring Data JPA 3.4+

### Rationale
- ✅ Virtual Threads en Java 21
- ✅ Records y sealed classes (DDD modeling)
- ✅ Spring 6.x performance improvements
- ✅ Spring Boot 3 native compilation (GraalVM ready)

### Consequences
- Legacy monolito seguirá con Java 8 (READ-ONLY)
- Dual version:legacy (Java 8) + new services (Java 21)
- Requiero developers con Java 21+ knowledge

---

## DEC-003: Usar PostgreSQL 15+ como BD relacional principal

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Legacy usa MySQL 5.7 (deprecado). Nuevos servicios necesitan BD moderna con:
- JSONB support (flexible schemas)
- Full-text search built-in (posible alternativa a Elasticsearch para startup)
- UUID native support
- Logical replication

### Decision
- **Producción:** PostgreSQL 15+ con replication
- **Development:** PostgreSQL en Docker (TestContainers)
- **Cache:** Redis 7+ para sesiones y search index cache
- **Búsqueda:** Elasticsearch para Search-Service (CQRS read model)

### Consequences
- Legacy MySQL nunca migrado (freeze)
- Nuevos servicios no hablan con MySQL
- Requiere Elasticsearch para Search Context
- Necesita DB migrations tool (Flyway)

---

## DEC-004: Domain-Driven Design + Bounded Contexts

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Monolito tiene código spaghetti sin límites claros. Necesitamos:
- Clarity sobre qué pertenece a cada dominio
- Independent escalability (Job-Service crece ≠ User-Service crece)
- Clear contracts entre servicios

### Decision
Descomponer JRecruiter en 5 Bounded Contexts:

1. **Jobs Context** — Ofertas, CRUD, publicación
2. **Users Context** — Empleadores, candidatos, auth
3. **Search Context** — Full-text, filtrado, ranking (CQRS read)
4. **Applications Context** — Solicitudes, tracking
5. **Notifications Context** — Emails, alertas async

Cada contexto:
- Posee su agregado root (Job, Employer, Application, etc.)
- Tiene su BD (data isolation)
- Se comunica vía REST/gRPC (sync) o Events (async)
- Es responsable de garantizar invariantes propios

### Consequences
- Data duplication entre servicios (caché/read models)
- Consistencia eventual (no ACID distribuido)
- Requiere event bus (RabbitMQ/Kafka)
- Más complejo que monolito, pero escalable

---

## DEC-005: Strangler Fig Pattern para migración

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
No podemos hacer big bang migration (riesgo). Legacy sigue generando revenue.
Necesitamos migración gradual:
- Coexist legacy y new services temporalmente
- Zero downtime
- Rollback posible en cualquier momento

### Decision
Implementar API Gateway (Kong o nginx) que:
- Rutas `/api/jobs/*` → Job-Service (cuando esté listo)
- Rutas `/api/jobs/*` → Legacy (en el interim)
- Gradualmente incrementar tráfico a servicios nuevos

Timeline:
1. Semana 1-2: Análisis + Planificación
2. Semana 3-5: Job-Service production-ready
3. Semana 6: Redirigir 10% tráfico → Job-Service
4. Semana 7: Redirigir 50% tráfico
5. Semana 8: Redirigir 100% tráfico
6. Semana 9: Mantener legacy como fallback 4 semanas
7. Semana 13: Retirar legacy (solo si stable)

### Consequences
- Complejidad temporal en API Gateway
- Datos duplicados temporalmente (eventual consistency)
- Requiere monitoring exhaustivo (dual traces)
- Pero zero-downtime + safe rollback

---

## DEC-006: Event-Driven Async entre servicios

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Servicios necesitan comunicación pero queremos:
- Baja acoplamiento (Job-Service no debe conocer Notification-Service URLs)
- Escalabilidad (no RPC que bloquea)
- Auditabilidad (¿quién cambió qué?)

### Decision
Usar RabbitMQ o Kafka para event broadcasting:

```
Job-Service:
  - JobCreated → publish event al bus
  - (no espera respuesta)

Search-Service:
  - Escucha JobCreated → indexar en Elasticsearch

Notification-Service:
  - Escucha JobCreated → enviar email a Employer
  - Escucha ApplicationSubmitted → enviar email a Candidate
```

### Consequences
- ✅ Servicios independientes
- ✅ Auditabilidad: todos los events quedan en broker
- ⚠️ Consistencia eventual (Job no existe aún en Search index)
- ⚠️ Idempotencia: si event duplicado, debe ser safe
- ⚠️ Deadletter queues para mensajes fallidos

---

## DEC-007: CI/CD Pipeline: GitHub Actions + Docker

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Necesitamos automatización para:
- Validar cada commit (tests, linting, security)
- Prevenir secretos en repo
- Build y push Docker images
- Deploy a staging automático

### Decision
GitHub Actions workflow:

```yaml
Stage 1: Lint → Checkstyle, SpotBugs
Stage 2: Build → Maven clean package
Stage 3: Test → JUnit5 + TestContainers
Stage 4: Security → OWASP Dependency Check, Trivy
Stage 5: Build Docker → Multi-stage Dockerfile
Stage 6: Push → GHCR (GitHub Container Registry)
Stage 7: Deploy Staging → Kubernetes
```

### Consequences
- ✅ Automated quality gates
- ✅ Security scanning en cada PR
- ✅ Fast feedback loop (< 10 minutos)
- Requiere GitHub Actions secrets para credentials

---

## DEC-008: Testing Strategy: Unit + Integration + Contract

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** github-copilot

### Context
Tests críticos para mantener confiabilidad en migración. Necesitamos:
- Domain logic correcta (unit tests)
- Adapter integration (integration tests)
- Contract between services (contract tests)

### Decision
- **Unit Tests:** JUnit5 + Mockito (test domain sin BD/HTTP)
- **Integration Tests:** TestContainers (PostgreSQL real en Docker)
- **Contract Tests:** Spring Cloud Contract o Pact
- **Target:** 80% code coverage (domain + application layers)

### Consequences
- ✅ Confianza en cambios
- ✅ Documentación viviente (tests = specs)
- ⚠️ Mantenimiento de fixtures y test data

---

## DEC-009: Inmutabilidad de Legacy = READ-ONLY

**Status:** ✅ ACCEPTED | **Date:** 2026-03-08 | **Decision Owner:** Cline

### Context
Legacy monolito en `/legacy/` es fuente de verdad para entender el dominio.
Si lo modificamos, confundimos a nuevos developers.

### Decision
- **Legacy código:** 100% READ-ONLY
- **Cambios:** Hacerlos en servicios nuevos, NO en legacy
- **Bug fixes:** En nuevos servicios, no in legacy
- **Git hooks:** blockear commits a /legacy/

### Consequences
- ✅ Clarity para toda una migración
- ✅ Legacy no diverge durante desarrollo
- ✅ Rollback plan claro (volver a legacy funciona)
- Requiere disciplina + git hooks

---