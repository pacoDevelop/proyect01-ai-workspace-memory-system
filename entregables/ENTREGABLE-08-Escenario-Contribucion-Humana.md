# ENTREGABLE 8: Escenario Contribución Humana Urgente

**Descripción:** Cómo un humano puede hacer cambios urgentes al sistema sin esperar el protocolo completo de agentes.

**Cas o Real:** Un security engineer detecta una vulnerabilidad crítica (CVE) en las dependencias y necesita aplicar un parche YA, sin esperar a que un agente lo complete.

---

## CONTEXTO DEL PROBLEMA

**Escenario:** Es viernes 17:45. Se publica una CVE crítica (CVSS 9.8) en una dependencia que usamos.

```
CVE-2025-1234: Remote Code Execution in express-validator
Affected versions: < 7.0.5
Our version: 7.0.3
Publicado: Hace 30 minutos
Exploit público: EN INTERNET AHORA

→ Necesitamos un parche en DESARROLLO EN LOS PRÓXIMOS 30 MINUTOS.
→ Los agentes tardarían 2-3 horas en procesar y ejecutar

→ SOLUCIÓN: Humano hace cambio urgente con protocolo simplificado
```

---

## FLUJO SIMPLIFICADO PARA HUMANOS

### Paso 1: Crear Rama de Emergencia

```bash
# En lugar del flujo normal, humano hace:
cd repo
git checkout -b hotfix/cve-2025-1234-express-validator
# (sin esperar a que agente lo haga)
```

### Paso 2: Actualizar Dependencia

```bash
# Actualizar package.json
npm install express-validator@7.0.5 --save

# Verificar
npm list express-validator
# express-validator@7.0.5

# Commit
git add package.json package-lock.json
git commit -m "security: fix CVE-2025-1234 in express-validator

CRITICAL SECURITY PATCH
- Updated express-validator 7.0.3 → 7.0.5
- Fixes RCE vulnerability (CVSS 9.8)
- CVE: https://nvd.nist.gov/vuln/detail/CVE-2025-1234
- Applied as emergency hotfix (human intervention)
- Tests passing: npm test ✓
- Deployed to production immediately after merge

Security Review: Required before production
Approval: [Specify who approved]
"
```

### Paso 3: Notificar Sistema (SIN esperar autentificación completa)

```bash
# En lugar de actualizar agent_lock.yaml (que es para agentes),
# humano crea URGENTE entry en signals.yaml directamente:

cat >> ai/memory/signals.yaml << 'EOF'
  
  - id: "SIG-URGENT-001"
    type: "security_patch_applied"
    from: "human-security-engineer"
    to: "any"
    message: "CRITICAL: CVE-2025-1234 patch applied. Updated express-validator 7.0.3→7.0.5. RCE vulnerability closed."
    created_at: "2025-03-08T17:45:00Z"
    urgent: true
    requires_immediate_review: true
    related_decision: null
    related_task: null
    security_impact: "CRITICAL - RCE vulnerability"
EOF
```

### Paso 4: Actualizar change_log.md Manualmente

```markdown
# Manually add entry (no esperar agente)

## [2025-03-08T17:45:30Z] HOTFIX: CVE-2025-1234 Security Patch

**Tipo:** security | hotfix | urgent  
**Cambio ID:** CFG-HOTFIX-001  
**Responsable:** juan.garcia (human security engineer)

### Problema Crítico
- CVE-2025-1234 RCE en express-validator
- CVSS Score: 9.8 (Crítico)
- Versión afectada: 7.0.3
- Exploit público: SÍ
- Tiempo urgencia: < 1 hora

### Solución Aplicada
```bash
npm install express-validator@7.0.5
```

### Archivos Modificados
- package.json (versionado)
- package-lock.json (generado)

### Testing
- Tests unitarios: ✅ PASS
- Integración: ✅ PASS
- Security tests: ✅ PASS

### Deployment
- Inmediato a producción após merge
- No esperar ciclo normal de agentes
- Comunicar a equipo de ops

### Reversión (si necesario)
```bash
npm install express-validator@7.0.3 --save
git revert [commit-hash]
```

### Notas
- Bypass del protocolo de agentes autorizados
- Justificación: Criticidad de CVE (RCE)
- Requiere revisión de seguridad inmediata
- Contactar: juan.garcia@company.com para preguntas
```

### Paso 5: Push a rama y Pull Request

```bash
# Push a rama hotfix
git push origin hotfix/cve-2025-1234-express-validator

# Crear PR con etiqueta URGENT
# El título debe notar que es intervención humana:
# [HUMAN] [URGENT] CVE-2025-1234: Express-validator RCE patch

# En descripción del PR:
"""
## HUMAN INTERVENTION ALERT

This PR was created and pushed by a human security engineer, not by an AI agent.

**Reason:** Critical CVE-2025-1234 (RCE) published with public exploit.

**Timeline:**
- Published: 2025-03-08 17:15Z
- Discovered: 2025-03-08 17:30Z  
- Patch applied: 2025-03-08 17:45Z
- Total time: 30 minutes

**Action required:**
- [ ] Security review (required, not optional)
- [ ] Code review (express-validator patch is simple)
- [ ] Run security tests
- [ ] Approve for immediate deployment
- [ ] Deploy to production

**Communication:**
- Notify: ops@company.com, security@company.com
- Slack: #security-incidents
"""
```

---

## DIFERENCIAS CON PROTOCOLO DE AGENTES

| Aspecto | Protocolo Agente | Protocolo Humano Urgente |
|--------|------------------|-------------------------|
| **Agent Lock** | Crear entry, pulso cada 15min | SKIP (no aplicable) |
| **Crear Tarea** | Crear TASK-{id} en tasks.yaml | SKIP (ya está en CVE lista externa) |
| **Claim Task** | `claimed_at`, `assigned_agent` | SKIP (directamente hacer cambio) |
| **Session file** | Crear session-*.md con logs | SKIP (solo commit message es suficiente) |
| **Tiempo mínimo** | 1+ hora de protocolo | 5-10 minutos total |
| **Notificación** | Signal normal | Signal con `urgent: true` |
| **Review** | Opcional (según task) | OBLIGATORIO siempre |
| **Testing** | Local en agent | Local en humano |

---

## VALIDACIÓN EN CI/CD PARA CAMBIOS HUMANOS

**Problema:** El sistema debe permitir cambios humanos urgentes PERO verificar que no sean abuso.

```bash
#!/bin/bash
# .github/workflows/human-intervention-check.yml

name: Check for Human Interventions
on: [pull_request]

jobs:
  check_human_changes:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: |
          # Detectar si commit fue hecho por humano (no por agente)
          AUTHOR=$(git log -1 --format='%aN')
          
          # Listar agentes autorizados
          AUTHORIZED_AGENTS=(
            "claude-agent-alpha"
            "claude-agent-beta"
            "system"
          )
          
          if [[ ! " ${AUTHORIZED_AGENTS[@]} " =~ " ${AUTHOR} " ]]; then
            echo "⚠️ HUMAN INTERVENTION detected: $AUTHOR"
            
            # Si es cambio en archivos sensibles
            if git diff HEAD~1 HEAD --name-only | grep -E "package.json|src/.*auth|db/.*migration"; then
              echo "✅ Cambio en archivos sensibles por humano"
              echo "→ Requiere revisión manual"
              
              # No bloquear, pero hacer notoriese
              echo "::warning::Human intervention in critical files"
            fi
          fi
```

---

## RESTRICCIONES (IMPORTANTE)

Humanos pueden cambiar URGENCIAs:
- ✅ Actualizar dependencias (npm/pip)
- ✅ Patches de seguridad
- ✅ Hotfixes en prod
- ✅ Cambios de configuración ambiente
- ✅ Notas en change_log.md

Humanos NO deben cambiar:
- ❌ Lógica de negocio (debe ser tarea formal)
- ❌ API contracts (debe ser tarea formal)
- ❌ Decisiones arquitectónicas sin DEC-{id}
- ❌ Cambios en spec del sistema (requiere PR formal)

---

## ESCALATE A AGENTE (DESPUÉS)

Una vez que la emergencia está contenida:

```bash
# Crear tarea formal para agente investigar
# En ai/memory/tasks.yaml:

- id: "TASK-EOC-001"
  title: "Post-incident review: CVE-2025-1234 express-validator patch"
  description: >
    Human security engineer applied emergency patch on 2025-03-08T17:45Z
    for CVE-2025-1234 (RCE). Need formal review:
    1. Verify patch is minimal and correct
    2. Add security tests for regression
    3. Update security_patterns.md with this CVE
    4. Document in decisions.md why this was emergency
  status: "pending"
  priority: "high"
  security_sensitive: true
  requires_security_review: true
  assigned_agent: null
  created_by: "human-security-engineer"
  created_at: "2025-03-08T18:30:00Z"
```

---

## TEMPLATE PARA HUMANOS

```markdown
# HUMAN INTERVENTION TEMPLATE

When you need to make an urgent change:

## 1. Identificar Urgencia
- [ ] Problema es verdaderamente urgente (< 1 hora para impacto)
- [ ] No puede esperar el ciclo normal de agentes (2-3 horas)
- [ ] Ejemplo: CVE, prod outage, data loss risk

## 2. Hacer Cambios
- [ ] Crear branch: `hotfix/{nombre-corto}`
- [ ] Hacer cambio mínimo (no scope creep)
- [ ] Commit message: Incluir contexto de urgencia
- [ ] Tests: Pasar localmente antes de push

## 3. Notificar Sistema
- [ ] Crear signal en signals.yaml con `urgent: true`
- [ ] Actualizar change_log.md con contexto
- [ ] Mencionar por qué fue intervención humana

## 4. Code Review
- [ ] Crear PR con título [HUMAN] [URGENT]
- [ ] Requerir al menos 2 revisores
- [ ] Esperar aprobación antes de merge

## 5. Escalate (Después)
- [ ] Crear TASK- formal para investigación completa
- [ ] Humano documentar en change_log.md qué aprendimos
- [ ] Añadir a security_patterns.md si fue CVE/sec issue

## No Olvides
- Comunicar al equipo (Slack, email)
- Documentar en change_log.md
- Crear signal urgente en signals.yaml
- Asignar tarea formal a agente para análisis completo
```

---

## CASOS DE USO REALES

### Caso 1: CVE Crítica (Verdaderamente Urgente ✅)

```
Humano: Security engineer aplica parche express-validator
Tiempo: 30 minutos start-to-finish
Justificación: RCE publicado, exploit en libre circulación
Protocolo: Simplificado (solo 5 pasos)
Review: Obligatorio
Resultado: Patch en prod en < 1 hora
```

### Caso 2: Database Corruption (Verdaderamente Urgente ✅)

```
Humano: DBA restaura backup de BD
Tiempo: 45 minutos (downtime mínimo)
Justificación: Losing data, sistema down, SLA en riesgo
Protocolo:  Emergencia
Review: Post-incident review (después)
Resultado: Sistema restaurado, business continúa
```

### Caso 3: "Necesito agregar feature urgente" (❌ NO URGENTE)

```
Humano: PM quiere cambiar orden de campos en API
Tiempo: "Necesito esto hoy"
Justificación: "Importante para cliente X"
VEREDICTO: ❌ NO es emergencia, debe ir por proceso normal
Acción: Crear TASK- formal, asignar a agente
Esperar: 2-3 horas completará el proceso correcto
```

---

## CONCLUSIÓN

**El sistema permite intervención humana urgente PERO con validaciones:**

- ✅ Humanos pueden actuar rápidamente para emergencias verdaderas
- ✅ El protocolo simplificado toma 5-10 minutos
- ✅ Todos los cambios quedan auditados en git + change_log.md + signals.yaml
- ✅ CI/CD detecta y valida cambios humanos
- ✅ Review es obligatorio (no puede skippear este paso)
- ✅ Post-incidente, se escala a agente para análisis formal

**Garantía:** Ninguna emergencia quedará sin documentación ni sin revisión. El  sistema es simultaneamente ágil (< 30 min para CVE) Y auditable (100% trazabilidad).
