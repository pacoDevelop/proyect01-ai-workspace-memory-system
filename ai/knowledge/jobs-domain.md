# JOBS DOMAIN — Resumen de dominio de Ofertas

> Última actualización: 2026-03-08T08:05:00Z | Actualizado por: cursor-gpt  
> Fuente detallada: `ai/memory/jobs-domain-analysis.md`

## ▸ Propósito

Este documento resume el **Job Context** extraído del monolito legacy y cómo se modela en el nuevo Job-Service usando DDD + Arquitectura Hexagonal.

## ▸ Elementos principales

- **Aggregate Root:** `Job` (oferta de empleo).
- **Value Objects clave:** `JobTitle`, `JobDescription`, `JobLocation`, `JobSalary`, `JobPostingStatus`, `OfferedBy`.
- **Invariantes:** estado de publicación coherente, rangos salariales válidos, localización consistente, relación correcta con Employer.
- **Eventos de dominio:** `JobPublishedEvent`, `JobClosedEvent`, `JobHeldEvent`, `JobResumedEvent`.

Para un análisis completo (mapeos desde legacy, ejemplos de invariantes y diagramas), consultar el shard de memoria:`ai/memory/jobs-domain-analysis.md`.

