# USER DOMAIN — Empleadores y Candidatos

> Última actualización: 2026-03-08T08:05:00Z | Actualizado por: cursor-gpt  
> Fuente detallada: `ai/memory/user-domain-analysis.md`

## ▸ Propósito

Describir el dominio de **usuarios** de JRecruiter tal y como se migrará a microservicios: separación entre cuenta de autenticación y dominios de negocio `Employer` y `Candidate`.

## ▸ Elementos principales

- **Bounded Contexts relacionados:** Users Context, Applications Context.
- **Aggregates clave:**
  - `UserAccount` (auth, credenciales, roles).
  - `Employer` (empresa que publica ofertas).
  - `Candidate` (persona que aplica a ofertas).
- **Value Objects típicos:** `Username`, `Email`, `PasswordHash`, `VerificationToken`, datos de contacto y registro fiscal.
- **Patrones de seguridad:** RBAC basado en roles, separación de responsabilidades entre auth y dominio, uso previsto de JWT/OAuth2.

Para el detalle completo (diagramas, invariantes, flujos de registro/verificación y migración desde tablas legacy), ver:`ai/memory/user-domain-analysis.md`.

