# ENTREGABLE 12: Auditoría de Seguridad Completa

**Descripción:** Checklist de auditoría manual recomendada para ejecutar semanalmente. Valida que el sistema de memoria está asegurado correctamente.

---

## AUDITORÍA SEMANAL (15 - 20 minutos)

### Bloque 1: Acceso a Repositorio (5 minutos)

```markdown
## Semana del [FECHA]

### Bloque 1: Git Access Control
- [ ] **1.1** Revisar últimos 20 commits: `git log --oneline -20`
  - ¿Todos tienen commit message descriptivo?
  - ¿Hay commits de usuarios no esperados?
  - Acción: Si hay commits incógnitas, investigar ASAP
  
- [ ] **1.2** Verificar branch protection en main
  - GitHub: Settings → Branches → main
  - ¿Están habilitadas protecciones?
  - ¿Se requieren pull request reviews?
  - ¿Se ejecuta CI/CD antes de merge?
  
- [ ] **1.3** Revisar permisos de acceso
  - ¿Quién tiene acceso push a main?
  - ¿Hay credentials expuestas en .github/workflows?
  - Revocar: Si encontraste access innecesario

### Bloque 2: Secrets Management (5 minutos)

- [ ] **2.1** Revisar GitHub Secrets
  - GitHub: Settings → Secrets and variables → Actions
  - ¿Todos los secrets están rotados < 90 days?
  - ¿Hay secrets sin usar (cleanup)?
  
- [ ] **2.2** Verificar que no hay hardcoded secrets en código
  ```bash
  git log -p --all | grep -E "password|api_key|secret|token" | head -20
  # Si encuentra algo, revoke inmediatamente
  ```

- [ ] **2.3** Revisar .env.example
  - ¿Está documentando qué variables son requeridas?
  - ¿NO contiene valores actuales?
  - Acción: Actualizar si cambió estructura

### Bloque 3: Dependency Vulnerabilities (5 minutos)

- [ ] **3.1** Ejecutar verificación de dependencias
  ```bash
  npm audit
  # Registrar: Critical/High vulnerability count
  ```
  
- [ ] **3.2** Revisar GitHub Dependabot  
  - GitHub: Security → Dependabot alerts
  - ¿Hay vulnerabilidades sin resolver?
  - Acción: Crear PR para cada vulnerabilidad
  
- [ ] **3.3** Revisar outdated packages
  ```bash
  npm outdated
  # Actualizar packages > 6 months old
  ```

---

## AUDITORÍA MENSUAL (45 - 60 minutos)

### Bloque 4: Code Security (20 minutos)

```markdown
### Bloque 4: Code Review for Security
- [ ] **4.1** SAST - Static Analysis
  ```bash
  npm install -g @microsoft/eslint-plugin-security
  npx eslint src/ --plugin security
  
  # Registrar: Vulnerabilidades encontradas
  # Acción: Remediar todos los HIGH/CRITICAL issues
  ```

- [ ] **4.2** Secrets Scanning
  ```bash
  # Usar herramienta de detección
  truffleHog --json --regex -r .
  # Si encuentra secrets: REVOKE INMEDIATAMENTE
  ```

- [ ] **4.3** OWASP Top 10 Manual Review
  ```
  Revisar código para estos patrones malignos:
  
  ✓ A01 - Broken Access Control
    grep -r "user_id" src/routes/ | head -20
    ¿Está verificando que user_id == req.user.id antes de acceder?
  
  ✓ A02 - Cryptographic Failures
    grep -r "crypto\|encrypt\|hash" src/ | head -20
    ¿Está usando bcrypt o similar para passwords?
  
  ✓ A03 - Injection
    grep -r "concatenat\|template string.*execute" src/ | head -20
    ¿Hay SQL queries sin parametrización?
  
  ✓ A05 - Broken Access Control  
    grep -r "middleware\|authentication\|authorize" src/ | head -20
    ¿Cada endpoint tiene auth/authz?
  
  ✓ A07 - Identification and Authentication Failures
    grep -r "jwt\|oauth\|session" src/ | head -20
    ¿Se está usando tokens/JWT correctamente?
  ```

### Bloque 5: Infrastructure Security (15 minutos)

- [ ] **5.1** Database Security
  - [ ] ¿Está usando conecciones SSL/TLS?
  - [ ] ¿Contraseña tiene > 20 caracteres?
  - [ ] ¿Está usando role-based access en BD?
  - [ ] ¿Hay backups automatizados?
  
- [ ] **5.2** Environment Variables
  - [ ] ¿Todos los secrets están en env vars, no en code?
  - [ ] ¿Hay .env local en .gitignore?
  - [ ] ¿Está usando env vars distinct para dev/prod?

- [ ] **5.3** Logging & Monitoring
  - [ ] ¿Se están loguando eventos de seguridad?
  - [ ] ¿Hay alertas para actividad sospechosa?
  - [ ] ¿Se está rotando logs (< 30 days en bucket)?

### Bloque 6: AI-Specific Security (15 minutos)

- [ ] **6.1** Agent Validation
  ```bash
  # En ai/memory/agent_profiles.yaml
  - ¿Todos los agentes tienen licenses correctas?
  - ¿Hay agentes con permisos innecesarios?
  - ¿Cambió algo desde última auditoría?
  ```

- [ ] **6.2** Task Security Tagging
  ```bash
  grep "security_sensitive:" ai/memory/tasks.yaml
  # ¿Todas las TASK con security_sensitive=true tienen review?
  # ¿Se completó el owasp_checklist para cada una?
  ```

- [ ] **6.3** Signal Audit
  ```bash
  tail -30 ai/memory/signals.yaml
  # ¿Hay signals "warning" o "alert" sin resolución?
  # ¿Se documentó contexto de cada warning?
  ```

- [ ] **6.4** Change Log Review
  ```bash
  grep -E "security|CVE|vulnerable|exploit" ai/memory/change_log.md
  # ¿Están documentadas todas las correcciones de seguridad?
  # ¿Se aplicó el parche correctamente?
  ```

### Bloque 7: Compliance & Documentation (10 minutos)

- [ ] **7.1** Security Patterns Update
  - [ ] ¿security_patterns.md está actualizado?
  - [ ] ¿Se agregaron nuevos patrones hallados?
  - [ ] ¿Se documentaron CVEs descubiertas?

- [ ] **7.2** Decisions Review
  - [ ] ¿Hay decisiones de seguridad (DEC-01x) documentadas?
  - [ ] ¿Se justificaron elecciones criptográficas?
  - [ ] ¿Se documentaron tradeoffs de seguridad vs performance?

- [ ] **7.3** Audit Trail
  - [ ] ¿Todo está en git (100% trazabilidad)?
  - [ ] ¿change_log.md registra cambios de seguridad?
  - [ ] ¿Puedes reconstruir qué pasó hace 3 meses?

---

## AUDITORÍA DE RESPUESTA A INCIDENTES (Si ocurre)

### Cuando se detecta un problema:

```markdown
## INCIDENT RESPONSE AUDITORÍA

**Incident ID:** INC-001  
**Fecha descubierto:** 2025-03-XX  
**Severidad:** [CRITICAL|HIGH|MEDIUM|LOW]

### Fase 1: Containment (Primeras 30 minutos)
- [ ] Se detuvo el compromiso?
- [ ] Se revocaron credenciales si fue necesario?
- [ ] Se alertó al equipo?
- __Documentación:__ change_log.md + signals.yaml

### Fase 2: Investigation (1-4 horas)
- [ ] Se identificó el vector de ataque?
- [ ] Se determinó scope: cuántos datos/funciones fueron afectadas?
- [ ] Se hizo timeline de los eventos?
- __Conclusiones:__ DEC-{id} con lecciones aprendidas

### Fase 3: Remediation (4-24 horas)
- [ ] Se aplicó el parche?
- [ ] Se testeó que el parche funciona?
- [ ] Se verificó que exploit ya no funciona?
- __Validación:__ PR + security tests passing

### Fase 4: Post-Incident (24-48 horas)
- [ ] Se documentó el incidente como DEC-{id}?
- [ ] Se creó TASK- para prevención futura?
- [ ] Se actualizo security_patterns.md?
- [ ] Se comunicó con stakeholders?
- __Documentación:__ Complete
```

---

## MATRIZ DE SEVERIDAD & RESPUESTA

| Evento | Severidad | Respuesta | Tiempo |
|--------|-----------|-----------|--------|
| CVE publicada (RCE) | CRITICAL | Patch + deploy en 1h | URGENTE |
| Access sin autorizar | CRITICAL | Revoke + rotation en 30min | URGENTE |
| Datos expostos | HIGH | Containment + audit + notify | < 4h |
| Dependency outdated  | MEDIUM | Update + test | < 1 week |
| Failed 2FA | MEDIUM | Reset password | < 24h |
| Weak API auth | LOW | Implement proper auth | < 1 sprint |

---

## CHECKLIST MENSUAL EJECUTIVO

Para C-level, entregar este resumen una vez al mes:

```markdown
# SECURITY & INTEGRITY MONTHLY REPORT

**Month:** March 2025  
**Prepared by:** Security Team  
**Status:** ✅ SECURE | ⚠️ DEGRADED | ❌ COMPROMISED  

## Executive Summary
- Total commits reviewed: 847
- Security issues found: 2 (both remediated)
- CVEs in dependencies: 0
- Access unauthorizado: 0
- Uptime: 99.97%

## Key Metrics
| Metric | Target | Actual | Status
|--------|--------|--------|--------
| Secret scanning | 0 found | 0 | ✅
| Dependency vulns | 0 critical | 0 | ✅
| Code coverage | > 80% | 87% | ✅
| Security tests | > 90% pass | 100% pass | ✅

## Incidents this month
- [None] | [Count: X] | [Details in INC-001, INC-002]

## Recommendations
1. Rotate OAuth tokens (scheduled for end of month)
2. Update Node.js runtime (current: 18.x, available: 20.x)
3. Add rate limiting to auth endpoints (in progress)

## Compliance
- [ ] OWASP Top 10: ✅ Compliant  
- [ ] Data Privacy: ✅ Compliant
- [ ] Audit Trail: ✅ 100% git history preserved
- [ ] Access Control: ✅ RBAC implemented

**Risk Level:** LOW  
**Recommend:** Continue current security posture with 3 recommendations above
```

---

## AUDITORÍA TRIMESTRAL (PENETRATION TEST)

Una vez cada trimestre, considerar:

```markdown
# Q1 2025 - Security Audit Action Items

- [ ] Realizar manual penetration test o contratar firm externa
- [ ] Ejecutar full SAST (Semgrep, Bandit, snyk)  
- [ ] Revisar logs de seguridad últimos 90 días
- [ ] Identificar patrones de ataque repetidamente encontrados
- [ ] Actualizar security_patterns.md basado en hallazgos
- [ ] Crear training para team basado en vulnerabilidades encontradas
- [ ] Revisary actualizar DEC- de componentes críticos
- [ ] Generar report para C-level / board / compliance team

**Entregables:**
1. Penetration test report (externo)
2. Security audit findings (interno)
3. Remediation roadmap (3-6 meses)
4. Executive summary (1 pág)
```

---

## TEMPLATE AUDITOR DIARIO

Para humano responsable de security:

```bash
#!/bin/bash
# security-audit-daily.sh
# Ejecutar cada
 mañana (o via cron)

AUDIT_DATE=$(date +%Y-%m-%d)
AUDIT_FILE="ai/audits/audit-${AUDIT_DATE}.log"

mkdir -p ai/audits

{
  echo "=== DAILY SECURITY AUDIT ==="
  echo "Date: $AUDIT_DATE"
  echo ""
  
  echo "1. GitHub Secrets Check"
  # Placeholder - require manual check en UI
  echo "  ✓ Review https://github.com/[user]/[repo]/settings/secrets"
  echo ""
  
  echo "2. Dependency Vulnerabilities"
  npm audit --prod 2>/dev/null | grep -E "vulnerabilities|packages"
  echo ""
  
  echo "3. Last 24h Commits"
  git log --oneline --since="24 hours ago" | head -10
  echo ""
  
  echo "4. Security-tagged Tasks"
  grep "security_sensitive: true" ai/memory/tasks.yaml | wc -l
  echo " tasks marked security-sensitive"
  echo ""
  
  echo "=== END AUDIT ==="
  echo "If any red flags: Escalate immediately"
  
} | tee "$AUDIT_FILE"
```

---

## CONCLUSIÓN

**Auditoría de seguridad en 3 capas:**

- ✅ **Semanal (15-20 min):** Git access, secrets, dependencies
- ✅ **Mensual (45-60 min):** Code review, OWASP, infrastructure  
- ✅ **Trimestral (2-3 días):** Penetration testing, compliance report

**Beneficios:**
1. Detección temprana de issues
2. Compliance auditability (100% documentado)
3. Team training continuo (basado en findings)
4. Executive visibility de postura de seguridad  
5. Incident response playbook ejecutado regularmente
