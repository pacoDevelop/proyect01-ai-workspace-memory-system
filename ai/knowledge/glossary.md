# GLOSSARY — Términos del Dominio JRecruiter

> Última actualización: 2026-03-08T04:45:00Z | Actualizado por: github-copilot

## ▸ TÉRMINOS CORE DEL NEGOCIO

**Job / Oferta:** Posición vacante publicada por Employer. Ciclo: draft → published → closed.

**Employer / Empleador:** Empresa o reclutador que publica ofertas de empleo.

**Candidate / Candidato:** Persona buscando empleo. Crea perfil, busca ofertas, se aplica.

**Application / Solicitud:** Cuando candidato se aplica a una job. Estados: pending → rejected/hired.

**Skill / Habilidad:** Competencia técnica o soft skill requerida o poseída.

**Salary / Salario:** Rango salarial (min, max, currency, period).

**Location / Localización:** Lugar de trabajo (ciudad, país, remote allowed).

## ▸ TÉRMINOS ARQUITECTÓNICOS

**Bounded Context:** Límite conceptual de un dominio. Cada uno es semi-independiente.

**Aggregate Root:** Entidad principal de un contexto (Job, Employer, Application).

**Port:** Interfaz de contrato (repositorio, servicio, publisher).

**Adapter:** Implementación concreta (PostgreSQL adapter, RabbitMQ publisher).

**Strangler Fig:** Patrón de migración gradual donde nuevo sustituye lentamente al viejo.

**CQRS:** Command Query Responsibility Segregation (escribir ≠ leer).

**Event Sourcing:** Registrar cambios como eventos inmutables en el tiempo.

## ▸ ESTADOS DE TAREAS

**pending:** No se ha comenzado aún.

**claimed:** Agente se compromete a trabajarla.

**in_progress:** Agente está activamente desarrollando.

**review:** Completada, esperando aprobación.

**done:** Aceptada y merged a main.

**blocked:** Esperando dependencia u obstáculo.

## ▸ IDENTIFICADORES

**TASK-XXX:** Identificador de tarea en tasks.yaml (e.g., TASK-047).

**DEC-XXX:** Decisión arquitectónica documentada en decisions.md (e.g., DEC-001).

**SIG-XXX:** Señal inter-agente en signals.yaml (e.g., SIG-051-REVIEW-REQ).

**JWT:** JSON Web Token (autenticación sin estado).

**DDD:** Domain-Driven Design.
