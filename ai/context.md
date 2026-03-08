# PROJECT CONTEXT
> Última actualización: 2026-03-08T04:20:00Z | Actualizado por: Gemini
> ℹ️ Este workspace fue inicializado automáticamente para la migración de JRecruiter.

## ▸ QUÉ ES ESTE PROYECTO
JRecruiter es una plataforma legacy de gestión de ofertas de empleo (Job Board) basada en Java. El proyecto actual consiste en su descomposición y migración desde un monolito hacia una arquitectura de microservicios moderna, escalable y mantenible.

## ▸ OBJETIVO ACTUAL
Migrar la lógica de negocio del monolito ubicado en `/legacy` hacia microservicios independientes utilizando **Arquitectura Hexagonal** (Domain-Driven Design, Ports & Adapters).

## ▸ ESTADO DEL SISTEMA
**Estado:** STABLE
**Motivo:** Estructura de carpetas definida y agentes de IA sincronizados. El código fuente original reside en `/legacy` como fuente de verdad.

## ▸ TAREAS PRIORITARIAS AHORA
1. **Análisis de Bounded Contexts:** Identificar los límites de los futuros microservicios (Jobs, Users, Search, Notifications).
2. **Setup de Infraestructura Base:** Crear el esqueleto del primer microservicio (Job-Service) con Java 21+ y Spring Boot 3.4+.
3. **Definición de Puertos de Dominio:** Extraer las interfaces de servicio de `/legacy` y adaptarlas al nuevo modelo de dominio.

## ▸ AGENTES ACTIVOS

### 🤖 GitHub Copilot
- **ID:** `github-copilot`
- **Modelo:** Claude Haiku 4.5
- **Rol:** `primary-assistant` (Implementador)
- **Especialidad:** Generación de código, refactorización, unit testing y documentación técnica.
- **Prioridad Máxima:** Crítica.

### 🤖 Cline
- **ID:** `cline`
- **Modelo:** Claude Sonnet 4.5
- **Rol:** `secondary-assistant` (Arquitecto)
- **Especialidad:** Diseño de sistemas, arquitectura hexagonal, resolución de problemas complejos e integración.
- **Prioridad Máxima:** Alta.

### 🤖 Gemini
- **ID:** `gemini-coordinator`
- **Modelo:** Gemini 3 Flash
- **Rol:** `coordinator`
- **Especialidad:** Orquestación de tareas, validación de reglas de negocio y mantenimiento de este contexto.

## ▸ ARCHIVOS CRÍTICOS
- `/legacy/`: Directorio **READ-ONLY**. Contiene el monolito original. No modificar bajo ninguna circunstancia.
- `/services/`: Directorio raíz para los nuevos microservicios.
- `**/domain/`: Capa núcleo. No debe tener dependencias de frameworks externos.

## ▸ REGLAS DE ESTE PROYECTO
1. **Inmutabilidad del Legado:** El código en `/legacy` solo se consulta. Las mejoras o correcciones se hacen directamente en los nuevos servicios.
2. **Arquitectura Hexagonal Estricta:** La lógica de dominio debe estar aislada. Los adaptadores (infraestructura) deben ser intercambiables sin afectar al core.
3. **Estrategia de Estrangulamiento:** Migrar funcionalidad pieza a pieza siguiendo el patrón "Strangler Fig".
4. **Validación de Diseño:** Toda implementación de código realizada por `github-copilot` debe alinearse con las directrices de diseño de `cline`.

## ▸ LECTURAS RECOMENDADAS SEGÚN TAREA
- **Entender el Dominio de Vacantes:** `/legacy/src/main/java/org/jrecruiter/model/Job.java`
- **Lógica de Persistencia Legacy:** `/legacy/src/main/resources/org/jrecruiter/model/Job.hbm.xml`
- **Servicios de Negocio:** `/legacy/src/main/java/org/jrecruiter/service/`