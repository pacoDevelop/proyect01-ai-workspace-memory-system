# TECH STACK — JRecruiter Microservices

> Última actualización: 2026-03-08T04:45:00Z | Actualizado por: github-copilot

## ▸ STACK BACKEND NUEVO

**Core Framework:**
- Java 21 LTS (sin soporte para Java 8/11)
- Spring Boot 3.4+ (sin Spring Boot 2.x)
- Maven 3.9+ o Gradle 8.x

**Persistencia:**
- PostgreSQL 15+ (relacional)
- Spring Data JPA + Hibernate
- Flyway 10+ (DB migrations)
- Redis 7+ (cache)

**APIs & Comunicación:**
- Spring Web MVC (REST)
- Spring Cloud OpenFeign (client HTTP)
- RabbitMQ 3.13+ o Kafka 3.7+ (async)

**Testing:**
- JUnit 5.10+
- Mockito 5.x
- TestContainers 1.19+ (PostgreSQL en Docker)
- AssertJ 3.25+ (fluent assertions)

**Security:**
- Spring Security 3.4
- JWT (jjwt 0.12+)
- OAuth 2.0
- BCrypt (password hashing)

**Observabilidad:**
- Micrometer + Prometheus
- Spring Boot Actuator
- Grafana (visualization)
- ELK Stack (logs)
- Jaeger (distributed tracing)

**DevOps:**
- Docker 25+ + Docker Compose 2.x
- GitHub Actions (CI/CD)
- Kubernetes (deployment)

## ▸ LEGACY STACK (READ-ONLY)

- Java 8-11
- Spring Framework 4.x-5.x
- Hibernate 4.x-5.x
- MySQL 5.7

**Status:** FROZEN. Solo lectura para análisis.

## ▸ POLÍTICA DE VERSIONING

- **Java:** Mínimo 21 LTS
- **Spring Boot:** 3.4+
- **PostgreSQL:** 15+
- **Compatibility:** Máximo N-2 versiones atrás
