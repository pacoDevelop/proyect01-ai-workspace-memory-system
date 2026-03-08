# OVERVIEW — JRecruiter Microservices Migration

> Última actualización: 2026-03-08T04:45:00Z | Actualizado por: github-copilot

## ▸ QUÉ ES JRECRUITER

JRecruiter es una **plataforma legacy de gestión de ofertas de empleo (Job Board)** basada en Java. Sistema monolítico en producción que maneja:
- Gestión de Empleadores y Vacantes
- Búsqueda inteligente de ofertas
- Aplicaciones de candidatos
- Notificaciones personalizadas
- Auditoría y reportes

## ▸ OBJETIVO MIGRACIÓN

Descomponer el monolito en **microservicios escalables** usando Arquitectura Hexagonal, DDD y Strangler Fig Pattern.

### Servicios Planeados
1. **Job-Service** (FASE 2)
2. **User-Service** (FASE 3)
3. **Search-Service** (FASE 4)
4. **Notification-Service** (FASE 5)

## ▸ ESTADO

✅ Workspace inicializado  
✅ Agentes registrados  
⏳ Tareas en queue
