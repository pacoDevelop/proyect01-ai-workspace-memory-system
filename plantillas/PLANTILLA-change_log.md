# CHANGE LOG — Registro append-only de todos los cambios significativos

> **Regla absoluta:** NUNCA editar o borrar entradas pasadas. Solo añadir al final.
> **Cuándo añadir:** Una vez por tarea completada. No registrar cambios intermedios.
> **Rotación:** Cuando supera 500 líneas, mover entradas > 90 días a `/ai/archive/`

---

## [{YYYY-MM-DD HH:MM}Z] TASK-{id} — {agent-name}

**Cambio:** {Descripción en una línea de qué se hizo}

**Archivos modificados:**
  - `{path/archivo}` — {descripción de qué cambió}
  - `{path/otro}` — {descripción}

**Motivo:** {Por qué se hizo. Qué problema resuelve.}

**Impacto:** {Qué puede verse afectado (positivo y negativo)}

**Reversión:** {Cómo deshacer: comando git, pasos manuales, o "N/A si no hay riesgo"}

**Decisión relacionada:** {DEC-xxx o "ninguna"}

---

## ETIQUETAS ESPECIALES

Estas etiquetas pueden añadirse al inicio del cambio para categorizar eventos importantes:

- `[MERGE-RESOLUTION]` — Resolución de conflicto en archivos /ai/
- `[REOPEN]` — Tarea reabierta desde done
- `[SECURITY-ALERT]` — Problema de seguridad detectado
- `[SECURITY-INCIDENT]` — Incidente de seguridad en producción (crítico)
- `[RECOVERY]` — Limpieza automática de estado corrupto o agente fantasma
- `[SCHEMA-MIGRATION]` — Migración de versión de schema
- `[ESCALATED]` — Tarea escalada automáticamente por tiempo sin avance

---

## EJEMPLO DE ENTRADA COMPLETA

```markdown
---
## [2025-03-07 14:30Z] TASK-047 — claude-agent-alpha

**Cambio:** Implementado endpoint POST /auth/refresh con single-use refresh tokens
**Archivos modificados:**
  - `src/auth/tokenService.js` — nueva función generateTokenPair()
  - `src/routes/auth.js` — nuevo endpoint /refresh con validación
  - `tests/auth/refresh.test.js` — 12 tests de cobertura (happy path + edge cases)
  - `docs/api.md` — documentado contrato de API /auth/refresh
**Motivo:** TASK-047. Resolver vulnerabilidad de revocación de tokens comprometidos.
**Impacto:** Endpoint funcional permite logout instantáneo sin esperar JWT expiración.
             Requiere que clientes móviles implementen lógica de refresh.
**Reversión:** git revert {commit-sha} && npm run test
**Decisión relacionada:** DEC-012 (OAuth2 con refresh tokens)
---
```

---

## ARCHIVADO AUTOMÁTICO

Cuando change_log.md supere 500 líneas:
1. Mover entradas con created_date > 90 días a `/ai/archive/change_log_YYYY-QN.md`
2. Mantener las últimas 100 líneas aquí como referencia reciente
3. Script de validación detecta automáticamente si es necesario archivado
