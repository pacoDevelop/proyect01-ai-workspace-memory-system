# ENTREGABLE 10: Escenario Señales Inter-Agente Completo

**Descripción:** Ciclo de vida completo de cómo dos agentes se comunican formalmente mediante signals.yaml

**Escenario Real:** TASK-051 requiere Review. claude-agent-alpha escribe una signal, claude-agent-beta lee y responde. Sin polling, solo eventos.

---

## TIMELINE: 08:00 - 12:00

### T=08:00: Estado Inicial

```yaml
# ai/memory/tasks.yaml

- id: "TASK-051"
  title: "Security: Update bcrypt version and test password reset"
  description: "Upgrade bcrypt from v3.8 to v5.0, add tests to verify compatibility"
  status: "in_progress"
  assigned_agent: "claude-agent-alpha"
  started_at: "2025-03-08T07:00:00Z"
  security_sensitive: true
  requires_security_review: true  # ← MUY IMPORTANTE

# ai/memory/agent_lock.yaml

agents:
  - id: "claude-agent-alpha"
    locked_files:
      - "ai/memory/tasks.yaml"
      - "package.json"
      - "src/auth/password.js"
    locked_at: "2025-03-08T07:00:00Z"
    heartbeat_at: "2025-03-08T08:00:00Z"
    current_task: "TASK-051"
    status: "working"
```

```yaml
# ai/memory/signals.yaml — Todavía vacío

signals: []  # Sin signals aún
```

---

## T=09:30: Alpha Completa el Trabajo (Signal 1)

### Alpha escribe cambios

```bash
# claude-agent-alpha local work

## 1. Actualiza package.json
npm install bcrypt@5.0.0 --save

## 2. Actualiza password code
# src/auth/password.js
- Old: const bcrypt = require('bcrypt');
+ New: const bcrypt = require('bcrypt@5.0.0');

## 3. Escribe tests
# tests/auth/password.test.js
describe('Password with bcrypt v5.0', () => {
  it('should hash passwords correctly', () => { ... })
  it('should verify passwords correctly', () => { ... })
})

## 4. Commit local
git add package.json src/auth/password.js tests/auth/password.test.js
git commit -m "security: upgrade bcrypt v3.8 → v5.0 (TASK-051)"

## 5. Push  
git push origin main
```

### Alpha crea Signal de Review Solicitado

```yaml
# CAMBIO EN ai/memory/signals.yaml

  - id: "SIG-051-REVIEW-REQ"
    type: "review_requested"
    from: "claude-agent-alpha"
    to: "claude-agent-beta"
    task_id: "TASK-051"
    
    message: "TASK-051 completed: bcrypt v3.8 → v5.0 upgrade. Requires security review before marking done."
    
    details:
      reason: "Security-sensitive code change (password hashing)"
      requires_review_type: "security"
      review_checklist:
        - "Verify bcrypt version is correctly updated"
        - "Check password hash/verify functions work correctly"
        - "Ensure backward compatibility with existing hashed passwords"
        - "No breaking changes in auth API"
        - "Tests passing"
      
    created_at: "2025-03-08T09:30:00Z"
    created_by_commit: "abc123def456"
    
    read_by: []  # Nadie ha leído aún
```

### Alpha actualiza Task  

```yaml
# CAMBIO EN ai/memory/tasks.yaml

- id: "TASK-051"
  title: "Security: Update bcrypt version and test password reset"
  status: "review"  # ← CAMBIO: in_progress → review
  assigned_agent: "claude-agent-alpha"
  
  completed_at: "2025-03-08T09:30:00Z"
  completion_notes: |
    - Bcrypt upgraded to v5.0.0
    - Tests added for hash/verify functions
    - Package.json updated
    - All tests passing locally
    - Awaiting security review from claude-agent-beta
  
  definition_of_done_check:
    - "bcrypt upgraded": true
    - "tests written": true
    - "tests passing": true
    - "security review": false  # ← Pendiente
    - "code reviewed": false    # ← Pendiente
```

### Alpha notifica con Commit

```bash
git add ai/memory/signals.yaml ai/memory/tasks.yaml
git commit -m "signal: request security review for TASK-051 (bcrypt upgrade)

Task completed:
- bcrypt v3.8 → v5.0 upgrade
- Tests written and passing
- Awaiting security review from claude-agent-beta

Signal: SIG-051-REVIEW-REQ
Reviewer: claude-agent-beta"

git push origin main
```

---

## T=10:00: Beta lee Task (signal detection)

### Beta tira pull (como hace cada 15min)

```bash
# claude-agent-beta routine check

git pull origin main

# Lee signals para entender qué agentes necesitan
cat ai/memory/signals.yaml

# RESULTADO:
# - SIG-051-REVIEW-REQ encontrado
#   - from: claude-agent-alpha
#   - to: claude-agent-beta (¡TA!)
#   - task_id: TASK-051
#   - type: review_requested
```

### Beta marca Signal como leída

```yaml
# CAMBIO EN ai/memory/signals.yaml

- id: "SIG-051-REVIEW-REQ"
  type: "review_requested"
  from: "claude-agent-alpha"
  to: "claude-agent-beta"
  task_id: "TASK-051"
  
  message: "TASK-051 completed: bcrypt v3.8 → v5.0 upgrade. Requires security review..."
  created_at: "2025-03-08T09:30:00Z"
  
  read_by:
    - {agent: "claude-agent-beta", at: "2025-03-08T10:00:00Z"}  # ← NUEVO
  
  read_by_count: 1
  read_at_first: "2025-03-08T10:00:00Z"
```

---

## T=10:05: Beta Revisa y Aprueba (Signal 2)

### Beta examina el trabajo

```bash
# claude-agent-beta review

# 1. Ver cambios
git show main:package.json | grep bcrypt
# bcrypt: ^5.0.0  ✅ Version correct

# 2. Revisar código
git diff HEAD~1 HEAD -- src/auth/password.js
# Cambios mínimos, solo bump versión ✅

# 3. Revisar tests
git diff HEAD~1 HEAD -- tests/auth/password.test.js
# Tests cubren hash/verify ✅
# Backward compatibility tested ✅

# 4. Run tests  
npm test -- tests/auth/password.test.js
# ✅ PASS: All 12 tests passing

# 5. Verificar SAST
npm run security
# ✅ PASS: No security issues detected

# 6. Check version compatibility
npm list bcrypt
# bcrypt@5.0.0 ✅
```

### Beta crea Signal de Aprobación

```yaml
# CAMBIO EN ai/memory/signals.yaml

  - id: "SIG-051-REVIEW-APPROVED"
    type: "review_approved"
    from: "claude-agent-beta"
    to: "claude-agent-alpha"
    task_id: "TASK-051"
    
    message: "TASK-051 security review APPROVED. Bcrypt upgrade is correct and secure."
    
    details:
      approval_date: "2025-03-08T10:05:00Z"
      approved_by: "claude-agent-beta"
      
      review_findings:
        - criterion: "version_correct"
          status: "PASS"
          comment: "bcrypt v5.0.0 correctly updated in package.json"
        
        - criterion: "code_minimal"
          status: "PASS"
          comment: "Zero breaking changes, only version bump"
        
        - criterion: "tests_complete"
          status: "PASS"
          comment: "12 tests cover hash/verify functions, backward compatibility"
        
        - criterion: "security_clean"
          status: "PASS"
          comment: "npm audit clean, no vulnerabilities, no breaking changes"
        
        - criterion: "backward_compatible"
          status: "PASS"
          comment: "Existing password hashes will work with v5.0"
      
      overall_status: "APPROVED"
      approval_type: "security_review"
    
    created_at: "2025-03-08T10:05:00Z"
    created_by_commit: "def456ghi789"
    
    read_by: []  # Alpha verá cuando tire pull
```

### Beta también actualiza la Tarea

```yaml
# CAMBIO EN ai/memory/tasks.yaml

- id: "TASK-051"
  status: "done"  # ← CAMBIO: review → done
  assigned_agent: "claude-agent-alpha"
  
  review_completed_at: "2025-03-08T10:05:00Z"
  reviewed_by: "claude-agent-beta"
  
  review_result: "APPROVED"
  review_notes: "Security review passed. bcrypt upgrade is correct and tested."
  
  definition_of_done_check:
    - "bcrypt upgraded": true
    - "tests written": true
    - "tests passing": true
    - "security review": true  # ✅ COMPLETADO
    - "code reviewed": true    # ✅ COMPLETADO
  
  final_status: "DONE"
  
  state_history:
    - { from: "in_progress", to: "review", at: "2025-03-08T09:30:00Z", by: "claude-agent-alpha" }
    - { from: "review", to: "done", at: "2025-03-08T10:05:00Z", by: "claude-agent-beta" }
```

### Beta commita todos los cambios

```bash
git add ai/memory/signals.yaml ai/memory/tasks.yaml
git commit -m "signal: approve security review for TASK-051

Review completed:
- Bcrypt version correctly updated
- Tests comprehensive and passing
- No security issues found
- Backward compatibility verified

Task: TASK-051 → DONE
Signal: SIG-051-REVIEW-APPROVED
Approval: claude-agent-beta"

git push origin main
```

---

## T=10:30: Alpha lee Aprobación (Signal 3)

### Alpha tira pull (como hace cada 15min)

```bash
# claude-agent-alpha routine check

git pull origin main

# Lee signals para ver si hay respuestas
cat ai/memory/signals.yaml

# RESULTADO:
# - SIG-051-REVIEW-APPROVED encontrado
#   - from: claude-agent-beta
#   - to: claude-agent-alpha (¡TA!)
#   - Status: APPROVED
```

### Alpha marca Signal como leída

```yaml
# CAMBIO EN ai/memory/signals.yaml

- id: "SIG-051-REVIEW-APPROVED"
  type: "review_approved"
  from: "claude-agent-beta"
  to: "claude-agent-alpha"
  
  message: "TASK-051 security review APPROVED..."
  created_at: "2025-03-08T10:05:00Z"
  
  read_by:
    - {agent: "claude-agent-alpha", at: "2025-03-08T10:30:00Z"}  # ← NUEVO
```

---

## T=11:00: Update change_log.md (Final notification)

```markdown
# ai/memory/change_log.md

## [2025-03-08T10:35:00Z] TASK-051: Bcrypt Security Upgrade Completed

**Cambio ID:** CHG-051  
**Tipo:** security | upgrade  
**Status:** DONE (security approved)

### Cambio
- Upgraded bcrypt from v3.8.0 to v5.0.0
- Added comprehensive tests for password hashing
- Verified backward compatibility with existing password hashes

### Archivos Modificados
- package.json (version bump)
- src/auth/password.js (minimal compatible update)
- tests/auth/password.test.js (new tests)

### Flow
1. claude-agent-alpha: Implementó upgrade + tests
2. Signal: SIG-051-REVIEW-REQ (requerimiento de review)
3. claude-agent-beta: Revisó, tests pasaron
4. Signal: SIG-051-REVIEW-APPROVED (aprobación)
5. Task: TASK-051 → DONE

### Signals Utilizados
- SIG-051-REVIEW-REQ (request)
- SIG-051-REVIEW-APPROVED (approval)

### Testing
- Unit tests: 12 passing
- Security scan: 0 issues
- Backward compatibility: Verified

### Reversión
```bash
git revert def456ghi789
npm install bcrypt@3.8.0 --save
```

### Notas
Ejemplo completo de coordinación entre agentes usando signals:
1. Sin polling
2. Sin shared state  
3. 100% basado en git + signals.yaml
```

---

## DIAGRAMA DE FLUJO

```
T=09:30: Alpha → SIG-051-REVIEW-REQ → "Necesito review"
          ↓
          signals.yaml actualizado

T=10:00: Beta lee (git pull) → Ve signal "para mí"
          ↓
          Revisa TASK-051 código

T=10:05: Beta → SIG-051-REVIEW-APPROVED → "Aprobado!"  
          ↓
          Beta también marca TASK como DONE

T=10:30: Alpha lee (git pull) → Ve "Aprobado"
          ↓
          Alpha sabe estar lista para próximas tareas

T=11:00: change_log.md registra flow completo
          ↓
          Historial auditable en git
```

---

## KEY INSIGHTS

### 1. Sin Polling

```python
# ❌ MAL: Beta checkea constantemente
while True:
  if has_review_request():
    review()
  time.sleep(60)  # Desperdicia CPU

# ✅ BIEN: Beta solo chequea cuando tira pull (cada 15 min)
git pull origin main
signals = read_signals()
for sig in signals where sig.to == "claude-agent-beta":
  respond()
```

### 2. Sin Estado Compartido

```python
# ❌ MAL: Shared Redis
redis_client.set("task:051:status", "review")

# ✅ BIEN: Solo archivos + signals
# Cada agente tiene su propia copy en memoria
# Sincronización via git push/pull
```

### 3. Completamente Auditado

```bash
# Toda la coordinación quedó registrada
git log --oneline | grep TASK-051
# abc123 security: upgrade bcrypt v3.8 → v5.0 (TASK-051)
# def456 signal: request security review for TASK-051
# ghi789 signal: approve security review for TASK-051
# jkl012 signal: update change_log for TASK-051

# Cambios visibles en diff
git show abc123 -- package.json
git show def456 -- ai/memory/signals.yaml
git show ghi789 -- ai/memory/tasks.yaml
```

---

## EXTENSIÓN: Múltiples Agentes

Si 5+ agentes trabajando en paralelo:

```yaml
# Signals pueden dirigirse a múltiples agentes

- id: "SIG-TEAM-NOTIFY"
  type: "info"
  from: "system"
  to: "any"  # ← Todos los agentes verán
  message: "TASK-051 completed and approved. Safe to deploy."
  
  read_by:
    - {agent: "claude-agent-alpha", ...}
    - {agent: "claude-agent-beta", ...}
    - {agent: "claude-agent-gamma", ...}
    - {agent: "claude-agent-delta", ...}
```

---

## CONCLUSIÓN

**Señales permiten coordinación asincrónica sin servidores:**

- ✅ Agnetes se comunican via signals.yaml
- ✅ Sin polling (solo lectura cada 15 minutos)
- ✅ Sin shared state (todo en git)
- ✅ 100% auditado (historial completo)
- ✅ Escalable a N agentes
- ✅ Recuperable (git revert si falla)

**Timeline típica:** 1-2 horas de ciclo completo (request → review → approval)
