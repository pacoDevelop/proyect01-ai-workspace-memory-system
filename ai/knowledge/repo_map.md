# REPO MAP — Estructura Física del Proyecto

> Última actualización: 2026-03-08T04:45:00Z | Actualizado por: github-copilot

## ▸ ESTRUCTURA RAÍZ

```
jrecruiter-migration/
├── legacy/                   # READ-ONLY ⛔ Monolito (fuente de verdad)
│   ├── src/main/java/org/jrecruiter/
│   │   ├── model/           # Job.java, Employer.java, Candidate.java
│   │   ├── service/         # JobService, UserService, SearchService
│   │   ├── controller/      # REST endpoints
│   │   └── persistence/     # Hibernate mappings
│   └── pom.xml             # Java 8/11, Spring 4/5 (FROZEN)
│
├── services/                 # NEW MICROSERVICES ✓
│   ├── job-service/
│   │   ├── src/main/java/com/jrecruiter/jobs/
│   │   │   ├── domain/         # Core logic (NO frameworks)
│   │   │   ├── application/    # Use cases + DTOs
│   │   │   ├── infrastructure/ # Adapters (BD, REST, Events)
│   │   │   └── config/         # Spring configuration
│   │   ├── src/test/           # JUnit5, Mockito, TestContainers
│   │   └── pom.xml            # Java 21, Spring Boot 3.4
│   ├── user-service/           # (estructura similar)
│   ├── search-service/         # (estructura similar)
│   └── notification-service/   # (estructura similar)
│
├── ai/                       # AI WORKSPACE ✓
│   ├── context.md           # Resumen ejecutivo
│   ├── tasks.yaml           # Grafo de tareas + dependencias
│   ├── agent_lock.yaml      # Mutex distribuido
│   ├── agent_profiles.yaml  # Registro de agentes IA
│   ├── signals.yaml         # Notificaciones inter-agente
│   ├── decisions.md         # Decisiones arquitectónicas (DEC-*)
│   ├── change_log.md        # Auditoría append-only
│   │
│   ├── knowledge/           # BASE DE CONOCIMIENTO
│   │   ├── overview.md      # Visión del proyecto
│   │   ├── architecture.md  # Diseño hexagonal
│   │   ├── tech_stack.md    # Stack tecnológico
│   │   ├── glossary.md      # Términos del negocio
│   │   └── repo_map.md      # Este archivo
│   │
│   ├── memory/
│   │   └── _index.md        # Índice de descobrimientos
│   │
│   ├── sessions/            # Bitácoras de agentes
│   │
│   ├── archive/             # Histórico rotado
│   │
│   └── scripts/
│       └── validate_workspace.sh
│
├── .github/workflows/       # CI/CD (GitHub Actions)
│   ├── build.yml
│   ├── test.yml
│   └── security.yml
│
├── README.md                # Getting started
├── EJECUTIVO.md             # Resumen para C-level
└── .gitignore
```

## ▸ ARCHIVOS CRÍTICOS

**Entender el dominio legacy:**
- `/legacy/src/main/java/org/jrecruiter/model/Job.java`
- `/legacy/src/main/java/org/jrecruiter/service/JobService.java`

**Nueva arquitectura:**
- `/ai/knowledge/architecture.md`
- `/ai/knowledge/tech_stack.md`
- `/ai/decisions.md`

**Coordinar trabajo:**
- `/ai/context.md` — estado del proyecto
- `/ai/tasks.yaml` — tareas pending/in_progress
- `/ai/agent_lock.yaml` — quién trabaja dónde

## ▸ REGLAS DE ORGANIZACIÓN

- **`/legacy/`:** Solo lectura. NUNCA modificar bajo ninguna circunstancia.
- **`/services/`:** Código nuevo. Cada servicio es independiente y auto-contenido.
- **`/ai/`:** Infraestructura de agentes. Mantener histórico (nunca borrar).
- **Commits:** Incluir TASK ID (e.g., "feat: Job domain aggregate (TASK-005)")
