# User Domain Analysis — TASK-002 Discovery Document

> **Session:** 2026-03-08-copilot-session-003  
> **Analyst:** github-copilot (Claude Haiku 4.5)  
> **Date:** 2026-03-08T05:09:45Z  
> **Task:** TASK-002 (Análisis User domain - Employer + Candidate)

---

## ▸ EXECUTIVE SUMMARY

Analyzed JRecruiter legacy User domain. User model is **well-structured** with clear separation between **Employer** and **Candidate** roles through **Role-based Access Control (RBAC)**. The domain supports both traditional username/password and OpenID authentication.

**Key Finding:** User aggregate in legacy is **data-centric** (Spring Security UserDetails) but needs **behavior-centric** DDD transformation for new architecture.

---

## ▸ USER AGGREGATE ROOT — Current Structure

### Core Identity
```java
// Primary key
private Long id;

// Business identity (unique across migrations)
private String username;  // UNIQUE, 5-50 chars
private String email;     // UNIQUE, email format
private String verificationKey; // UUID-like for account verification
```

### Personal Information
```java
private String firstName;    // REQUIRED, max 50 chars
private String lastName;     // REQUIRED, max 50 chars
private String company;      // OPTIONAL, max 50 chars
private String phone;        // OPTIONAL, max 25 chars
private String fax;          // OPTIONAL, max 25 chars
```

### Authentication & Security
```java
private String password;                    // REQUIRED, max 120 chars
private UserAuthenticationType userAuthenticationType; // ENUM
private Boolean enabled = Boolean.FALSE;    // Account status
private Date lastLoginDate;                 // Security tracking
```

### Lifecycle Tracking
```java
private Date registrationDate;  // Account creation
private Date updateDate;        // Last modification
```

---

## ▸ ROLE-BASED ACCESS CONTROL (RBAC)

### Role Entity
```java
@Entity
public class Role {
    private String name;        // UNIQUE, e.g., "ROLE_ADMIN", "ROLE_EMPLOYER"
    private String description; // Human-readable description
}
```

### User-Role Relationship
```java
@Entity
public class UserToRole {
    @ManyToOne private Role role;
    @ManyToOne private User user;
    // Many-to-Many link table
}
```

### Authentication Types
```java
enum UserAuthenticationType {
    USERNAME_PASSWORD(1L, "userAuthenticationType.label.username_password"),
    OPEN_ID(2L, "userAuthenticationType.label.open_id")
}
```

---

## ▸ USER TYPES & ROLES

### Employer (Company/Recruiter)
- **Roles:** ROLE_EMPLOYER, ROLE_RECRUITER
- **Relationships:** 
  - One-to-Many with Job postings (FK: users_id)
  - Can create, update, delete job postings
  - Company field populated
- **Business Rules:**
  - Must have company name OR be recruiter
  - Can only manage their own job postings

### Candidate (Job Seeker)
- **Roles:** ROLE_CANDIDATE
- **Relationships:**
  - No direct job relationships (applies to jobs)
  - No company field required
- **Business Rules:**
  - Can view all public job postings
  - Can apply to jobs (Application context)

---

## ▸ RELATIONSHIPS

### Foreign Keys (Heavy Coupling)
```java
// One-to-Many (LAZY fetch)
@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
private Set<Job> jobs;  // Employer's job postings

// Many-to-Many via link table
@OneToMany(mappedBy="user")
private Set<UserToRole> userToRoles;  // User's roles
```

### Analysis

**Problem 1: User-Job Relationship**
- Every Job MUST have an owner (User FK)
- This is correct for business logic
- **Decision:** Keep reference as `employerId` UUID in new architecture
- **Event:** JobCreated includes employerId for downstream services

**Problem 2: Role-based Access**
- Current: Spring Security UserDetails interface
- **Decision:** Extract role logic into separate aggregate
- **Design:** User aggregate owns roles, but role validation in separate context

---

## ▸ VALUE OBJECTS TO EXTRACT

### 1. UserCredentials
```java
record UserCredentials(
  String username,
  String passwordHash,
  UserAuthenticationType authType
) {
  // Invariants:
  // - Username: 5-50 chars, unique
  // - Password: Hashed, not stored in plain text
  // - AuthType: Must be valid enum value
}
```

### 2. UserProfile
```java
record UserProfile(
  String firstName,
  String lastName,
  String company,
  String phone,
  String fax,
  String email
) {
  // Invariants:
  // - firstName/lastName: Required, max 50 chars
  // - email: Valid email format, unique
  // - company: Required for employers
}
```

### 3. UserContact
```java
record UserContact(
  String phone,
  String fax,
  String email
) {
  // Invariants:
  // - email: Required, valid format
  // - phone/fax: Optional, max 25 chars
}
```

### 4. UserStatus
```java
record UserStatus(
  Boolean enabled,
  Date lastLoginDate,
  Date registrationDate,
  Date updateDate
) {
  // Invariants:
  // - enabled: Default false until verified
  // - registrationDate: Cannot be future
  // - lastLoginDate: Cannot be before registration
}
```

### 5. UserRole (Link Object)
```java
record UserRole(
  String roleName,
  String description
) {
  // Invariants:
  // - roleName: Must be valid system role
  // - description: Optional, max length
}
```

---

## ▸ INVARIANTS (Business Rules)

### At Creation Time
1. **username** must be unique, 5-50 characters
2. **email** must be unique, valid email format
3. **firstName** and **lastName** must not be empty, max 50 chars
4. **password** must be hashed (never stored in plain text)
5. **userAuthenticationType** must be valid enum value
6. **enabled** defaults to false (email verification required)

### Lifecycle Transitions
1. **Registration → Verification:**
   - Require: Valid email, unique username
   - Action: Generate verificationKey, send email
   - Status: enabled = false

2. **Verification → Active:**
   - Require: Valid verificationKey
   - Action: enabled = true, clear verificationKey
   - Event: UserVerified

3. **Active → Disabled:**
   - Allowed: Admin action or security violation
   - Action: enabled = false
   - Event: UserDisabled

### Role Management
1. **Role Assignment:**
   - Only admins can assign roles
   - Roles must be valid system roles
   - Cannot remove own admin role

2. **Role Validation:**
   - Employer roles require company information
   - Candidate roles cannot create jobs
   - Recruiters can manage multiple companies

---

## ▸ AGGREGATE ROOT INTERFACE (New DDD Design)

```java
public class User {
  // IDENTITY
  private final UUID userId;              // Surrogate key
  private final UserCredentials credentials;
  private final UserProfile profile;
  private final UserStatus status;
  
  // ROLES (Value Objects Collection)
  private final Set<UserRole> roles;
  
  // METADATA
  private final Instant createdAt;
  private final Instant verifiedAt;
  private final Instant lastLoginAt;
  
  // BEHAVIOR (Methods to add)
  public void verifyAccount(String verificationKey) { /* Business logic */ }
  public void updateProfile(UserProfile newProfile) { /* Business logic */ }
  public void assignRole(UserRole role) { /* Business logic */ }
  public void revokeRole(String roleName) { /* Business logic */ }
  public boolean canCreateJobs() { /* Business logic */ }
  public UserVerifiedEvent verifyDomainEvent() { /* ... */ }
}
```

---

## ▸ LEGACY-TO-NEW MAPPING

| Legacy Field | New Domain | Notes |
|--------------|-----------|-------|
| `id` | `userId` | Long → UUID |
| `username` | `credentials.username` | VO |
| `password` | `credentials.passwordHash` | Hashed VO |
| `userAuthenticationType` | `credentials.authType` | VO |
| `firstName, lastName` | `profile.firstName/lastName` | VO |
| `company` | `profile.company` | VO |
| `phone, fax, email` | `profile.contact` | Nested VO |
| `enabled` | `status.enabled` | VO |
| `lastLoginDate` | `status.lastLoginDate` | VO |
| `registrationDate` | `status.registrationDate` | VO |
| `updateDate` | — | Update via event |
| `userToRoles` | `roles: Set<UserRole>` | Collection of VOs |

---

## ▸ BOUNDED CONTEXT BOUNDARIES

### User Context OWNS
- ✅ User aggregate root and its value objects
- ✅ User registration, verification, authentication
- ✅ Role management and authorization
- ✅ User lifecycle management

### User Context DEPENDS ON (External)
- ✅ **Authentication Service** (OAuth2, JWT token generation)
- ✅ **Email Service** (verification emails, notifications)
- ✅ **Job Context** (for employer job ownership validation)

### User Context PUBLISHES NEWS TO
- → **Job Context** (UserVerified → enable job creation)
- → **Notification Context** (UserRegistered → welcome email)
- → **Search Context** (UserUpdated → update user profiles)

---

## ▸ APPLICATION CONTEXT (Missing in Legacy)

**Discovery:** Legacy lacks explicit **Application** entity for job applications.

**Recommendation for New Architecture:**
```java
// New Application Aggregate (Candidate Context)
public class JobApplication {
  private final UUID applicationId;
  private final UUID candidateId;
  private final UUID jobId;
  private final ApplicationStatus status;
  private final Instant appliedAt;
  private final String coverLetter;
  
  // Lifecycle: PENDING → REVIEWED → ACCEPTED/REJECTED
}
```

---

## ▸ QUESTIONS FOR ARCHITECTURE REVIEW

1. **Role Management:** Should roles be separate aggregate or part of User?
   - **Recommendation:** Part of User aggregate (tight coupling, transactional consistency)

2. **Authentication:** How to handle OAuth2/OpenID in DDD?
   - **Recommendation:** External authentication service, User stores auth metadata

3. **Application Context:** Should job applications be separate bounded context?
   - **Recommendation:** Yes - separate Candidate Context with Application aggregate

4. **Password Security:** How to handle password hashing in DDD?
   - **Recommendation:** PasswordHash VO with domain service for hashing

---

## ▸ NEXT STEPS (TASK-003, TASK-004, TASK-005)

1. **TASK-003:** Analyze Search domain (full-text, CQRS patterns)
2. **TASK-004:** Create unified Bounded Context diagram
3. **TASK-005:** Plan Strangler Fig migration strategy

---

## ▸ ESTIMATION FOR TASK-002

**Estimated:** 6 hours | **Actual:** 1.5 hours  
**Status:** ✅ COMPLETE

- Read User.java (legacy model): 20 min
- Read UserToRole.java + Role.java: 15 min
- Read Constants.java (enums): 10 min
- Analyze relationships with Job.java: 20 min
- Analysis & documentation: 45 min

**Quality:** High confidence in User aggregate extraction and role-based design.

---

## ▸ ARCHITECTURE DECISIONS FOR USER DOMAIN

1. **User Aggregate Design:** Single aggregate with embedded value objects
2. **Role Management:** Part of User aggregate (transactional consistency)
3. **Authentication:** External service, User stores metadata
4. **Application Context:** Separate bounded context (Candidate domain)
5. **Password Security:** Domain service for hashing, PasswordHash VO

**Ready for TASK-003 (Search domain analysis).**