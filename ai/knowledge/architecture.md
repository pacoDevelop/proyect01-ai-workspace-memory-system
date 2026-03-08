# ARCHITECTURE — Hexagonal Design & Microservices

> Última actualización: 2026-03-08T04:45:00Z | Actualizado por: github-copilot

## ▸ PATRÓN BASE: ARQUITECTURA HEXAGONAL

Cada microservicio sigue **Ports & Adapters** (Alistair Cockburn):
- **Domain Core:** Sin dependencias de frameworks (Java puro)
- **Primary Ports (entrantes):** APIs REST, gRPC
- **Secondary Ports (salientes):** Base de datos, Message Bus, External APIs
- **Adapters:** Implementaciones concretas (PostgreSQL, RabbitMQ, HTTP, etc.)

## ▸ BOUNDED CONTEXTS (Domain-Driven Design)

### 1. Jobs Context
- **Agregado Root:** Job
- **Ciclo de vida:** draft → published → closed
- **Validaciones:** horarios, localización, salario

### 2. Users Context
- **Agregados:** Employer, Candidate
- **Autenticación:** OAuth2 + JWT
- **Perms:** Roles y permissions

### 3. Search Context
- **Búsqueda:** Full-text (Elasticsearch)
- **Filtrado:** Salario, localización, skills
- **Patterns:** CQRS (write a Jobs, read en Search)

### 4. Applications Context
- **Agregado:** Application
- **Estados:** pending → interviewing → rejected/hired

### 5. Notifications Context
- **Canales:** Email, in-app, SMS
- **Async:** Event bus (RabbitMQ/Kafka)

## ▸ PATRÓN STRANGLER FIG

Migración sin big bang:
1. Job-Service extrae funcionalidad
2. API Gateway redirige requests
3. Monolito gradualmente se reduce
4. Finalmente: monolito retirado

## ▸ FLUJO DE DATOS

- **Sync:** REST/gRPC (service-to-service)
- **Async:** RabbitMQ/Kafka (event streams)
- **Entrypoint:** API Gateway
- **Cache:** Redis (search index cache)
