## CONTEXT DEPENDENCIES — Mapa de Bounded Contexts

> Última actualización: 2026-03-08T08:05:00Z | Actualizado por: cursor-gpt  
> Fuente detallada: `ai/memory/context-dependencies-analysis.md`

## ▸ Propósito

Resumir cómo se relacionan los bounded contexts de JRecruiter (Jobs, Users, Search, Applications, Notifications), qué eventos comparten y qué dependencias existen entre ellos.

## ▸ Mapa de dependencias (alto nivel)

- **Jobs Context**
  - Publica eventos: `JobPublished`, `JobUpdated`, `JobClosed`.
  - Consumido por: Search-Service (indexado), Notification-Service (emails), Applications Context (reglas de elegibilidad).

- **Users Context**
  - Publica eventos: `UserRegistered`, `EmployerVerified`.
  - Consumido por: Jobs (para validar Employer), Notifications (para emails de bienvenida, alertas).

- **Applications Context**
  - Publica eventos: `ApplicationSubmitted`, `ApplicationStatusChanged`.
  - Consumido por: Notifications (emails a candidatos y empleadores).

- **Search Context**
  - Solo consume eventos (principalmente de Jobs) y mantiene un read model optimizado para búsqueda.

Para el diagrama completo, contratos DTO/eventos y estrategias de versionado de eventos, consultar:`ai/memory/context-dependencies-analysis.md`.

