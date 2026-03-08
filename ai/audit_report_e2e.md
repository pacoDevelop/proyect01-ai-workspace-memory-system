# Informe de Auditoría E2E — Hallazgos Técnicos de Integración

**Fecha:** 2026-03-08
**Auditor:** Antigravity (Gemini)
**Scope:** Integración entre microservicios, flujo de eventos y Gateway.

## 1. Discrepancias de Infraestructura (Servicios Fantasma)

Se ha detectado que los servicios reportados como "100% completados" carecen de los archivos de construcción y configuración necesarios.

| Servicio | Java Src | pom.xml | application.yml | Dockerfile | Migraciones SQL |
|----------|----------|---------|-----------------|------------|-----------------|
| Job-Service | ✅ | ✅ | ✅ | ✅ | ✅ |
| User-Service | ✅ | ❌ | ❌ | ❌ | ❌ |
| Search-Service | ✅ | ❌ | ❌ | ❌ | ❌ |
| Notification-Service | ✅ | ❌ | ❌ | ❌ | ❌ |

**Impacto:** Los servicios secundarios existen como lógica purista pero no son desplegables ni testeables en entornos reales/integrados.

---

## 2. Rotura en el Bus de Eventos (RabbitMQ)

Existe una desincronización crítica en los nombres de las colas entre el emisor y los receptores.

### Flujo: Job → Search
- **Productor (Job-Service):** 
    - Exchange: `job-events`
    - Cola configurada: `job-search-queue`
- **Consumidor (Search-Service):** 
    - Listener `@RabbitListener`: `job.published.queue`
    - **FALLO:** El consumidor escucha una cola que no está vinculada al exchange por el productor.

### Flujo: Job → Notification
- **Productor (Job-Service):** 
    - Exchange: `job-events`
    - Cola configurada: `job-notification-queue`
- **Consumidor (Notification-Service):** 
    - Listener `@RabbitListener`: `notification.job.created`
    - **FALLO:** El emisor envía a una cola genérica mientras el consumidor espera una cola específica no existente en el `RabbitMQConfig` del Job-Service.

---

## 3. Ausencia de API Gateway (Strangler Fig)

A pesar de que las decisiones arquitectónicas (DEC-005) y los documentos de análisis (TASK-004) detallan la configuración de rutas `/api/v2/`, no se ha encontrado:
- Directorio de Gateway (Kong/Nginx).
- Archivos de configuración de enrutamiento.
- Docker-compose que orqueste la entrada de tráfico.

---

## 4. Análisis de Auditorías Previas

Las tareas TASK-031 a TASK-036 dieron el visto bueno ("Quality Score 100/100") basándose únicamente en la revisión de los archivos `.java`. 
- **Error sistémico:** Los auditores anteriores no verificaron la integridad del artefacto completo (build files) ni la interconectividad real, limitándose al análisis estático del código de dominio.

## Conclusión

El sistema se encuentra en un estado de **Fragmentación Lógica**. Los componentes individuales son de alta calidad técnica (DDD), pero la infraestructura de red y despliegue que los une es inexistente o incoherente.

**Nivel de Riesgo:** 🔴 CRÍTICO
