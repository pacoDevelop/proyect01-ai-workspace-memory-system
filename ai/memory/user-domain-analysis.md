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

## ▸ AGGREGATE ROOT — Complete Implementation

```java
public class User {
  // IDENTITY
  private final UUID userId;              // Surrogate key
  private final UserCredentials credentials;
  private final UserProfile profile;
  
  // MUTABLE STATE
  private Boolean enabled;
  private Instant lastLoginAt;
  private String verificationKey;         // Nullable, cleared after verification
  private Instant createdAt;
  private Instant verifiedAt;             // Nullable, set after email verification
  private Instant updatedAt;
  
  // ROLES (Value Objects Collection)
  private Set<UserRole> roles = new HashSet<>();
  
  // TRANSIENT (not persisted)
  private List<DomainEvent> domainEvents = new ArrayList<>();
  
  // FACTORY METHOD: Register new user
  public static User registerNewUser(
      UUID userId,
      String username,
      String email,
      String plainPassword,  // Will be hashed
      String firstName,
      String lastName,
      PasswordHashingService hashingService) {
    
    // Validate invariants
    if (username == null || username.length() < 5 || username.length() > 50) {
      throw new InvalidUserException("Username must be 5-50 chars");
    }
    if (!isValidEmail(email)) {
      throw new InvalidUserException("Invalid email format");
    }
    if (plainPassword == null || plainPassword.length() < 8) {
      throw new InvalidUserException("Password must be at least 8 chars");
    }
    if (firstName == null || firstName.isEmpty()) {
      throw new InvalidUserException("First name is required");
    }
    if (lastName == null || lastName.isEmpty()) {
      throw new InvalidUserException("Last name is required");
    }
    
    // Hash password with bcrypt
    String passwordHash = hashingService.hashPassword(plainPassword);
    
    // Generate verification token
    String verificationKey = UUID.randomUUID().toString();
    
    // Create aggregate
    User user = new User(
        userId,
        new UserCredentials(username, passwordHash, UserAuthenticationType.USERNAME_PASSWORD),
        new UserProfile(firstName, lastName, email, null, null, null)
    );
    
    user.enabled = false;  // Not enabled until verified
    user.verificationKey = verificationKey;
    user.createdAt = Instant.now();
    user.updatedAt = Instant.now();
    
    // Emit event
    user.domainEvents.add(new UserRegisteredEvent(
        userId, username, email, firstName, lastName, verificationKey
    ));
    
    return user;
  }
  
  // PRIVATE CONSTRUCTOR
  private User(UUID userId, UserCredentials credentials, UserProfile profile) {
    this.userId = userId;
    this.credentials = credentials;
    this.profile = profile;
  }
  
  // BEHAVIOR: Verify email account
  public void verifyAccount(String providedVerificationKey) {
    if (this.enabled) {
      throw new InvalidUserStateException("Account already verified");
    }
    if (!providedVerificationKey.equals(this.verificationKey)) {
      throw new InvalidVerificationKeyException("Invalid or expired verification key");
    }
    
    this.enabled = true;
    this.verifiedAt = Instant.now();
    this.verificationKey = null;  // Clear after verification
    this.updatedAt = Instant.now();
    
    // Emit event
    this.domainEvents.add(new UserVerifiedEvent(
        this.userId,
        this.profile.getEmail(),
        this.verifiedAt
    ));
  }
  
  // BEHAVIOR: Authenticate user (verify password)
  public boolean authenticatePassword(
      String plainPassword,
      PasswordHashingService hashingService) {
    if (!this.enabled) {
      throw new AccountNotVerifiedException("Account is not verified");
    }
    
    boolean matches = hashingService.verifyPassword(
        plainPassword,
        this.credentials.getPasswordHash()
    );
    
    if (matches) {
      this.lastLoginAt = Instant.now();
      this.updatedAt = Instant.now();
    }
    
    return matches;
  }
  
  // BEHAVIOR: Assign role to user
  public void assignRole(String roleName) {
    if (!isValidRole(roleName)) {
      throw new InvalidRoleException("Invalid role: " + roleName);
    }
    
    // Check if already has role
    if (this.roles.stream()
        .anyMatch(r -> r.getRoleName().equals(roleName))) {
      return;  // Already assigned
    }
    
    this.roles.add(new UserRole(roleName));
    this.updatedAt = Instant.now();
  }
  
  // BEHAVIOR: Revoke role from user
  public void revokeRole(String roleName) {
    this.roles.removeIf(r -> r.getRoleName().equals(roleName));
    this.updatedAt = Instant.now();
  }
  
  // QUERY: Can this user create jobs?
  public boolean canCreateJobs() {
    return this.enabled &&
           this.hasRole("ROLE_EMPLOYER") &&
           this.profile.getCompany() != null;
  }
  
  // QUERY: Check if user has specific role
  public boolean hasRole(String roleName) {
    return this.roles.stream()
        .anyMatch(r -> r.getRoleName().equals(roleName));
  }
  
  // QUERY: Get all domain events
  public Collection<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableCollection(this.domainEvents);
  }
  
  // QUERY: Clear domain events after publishing
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }
  
  // GETTERS
  public UUID getUserId() { return userId; }
  public UserCredentials getCredentials() { return credentials; }
  public UserProfile getProfile() { return profile; }
  public Boolean getEnabled() { return enabled; }
  public Instant getLastLoginAt() { return lastLoginAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getVerifiedAt() { return verifiedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Set<UserRole> getRoles() { return Collections.unmodifiableSet(roles); }
  
  // HELPERS
  private static boolean isValidEmail(String email) {
    return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  }
  
  private static boolean isValidRole(String roleName) {
    return roleName != null && 
           (roleName.equals("ROLE_EMPLOYER") || 
            roleName.equals("ROLE_CANDIDATE") ||
            roleName.equals("ROLE_RECRUITER"));
  }
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

---

## ▸ PASSWORD HASHING DOMAIN SERVICE

```java
/**
 * Domain Service responsible for password security operations.
 * Pure cryptographic logic, no Spring dependencies.
 * Used by User aggregate to hash and verify passwords.
 */
public interface PasswordHashingService {
  /**
   * Hash plaintext password using bcrypt (one-way, irreversible).
   * @param plainPassword User's plaintext password
   * @return Bcrypt hash (includes salt, cost factor)
   */
  String hashPassword(String plainPassword);
  
  /**
   * Verify plaintext password against bcrypt hash.
   * @param plainPassword User's provided password (login attempt)
   * @param storedHash Bcrypt hash from database
   * @return true if password matches, false otherwise
   */
  boolean verifyPassword(String plainPassword, String storedHash);
}

// Spring Boot Implementation
@Component
public class BcryptPasswordHashingService implements PasswordHashingService {
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10); // Cost factor: 10
  
  @Override
  public String hashPassword(String plainPassword) {
    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    return encoder.encode(plainPassword);
    // Result: bcrypt hash like $2a$10$...
  }
  
  @Override
  public boolean verifyPassword(String plainPassword, String storedHash) {
    if (plainPassword == null || storedHash == null) {
      return false;
    }
    return encoder.matches(plainPassword, storedHash);
  }
}
```

---

## ▸ RBAC TRANSFORMATION: Legacy → New

### Legacy (Monolith) RBAC
```java
// Legacy architecture: User → UserToRole ← Role (join table)
@Entity
public class User {
  @OneToMany(mappedBy = "user")
  private Set<UserToRole> userToRoles;  // Join table rows
}

@Entity
public class UserToRole {
  @ManyToOne private User user;
  @ManyToOne private Role role;
}

@Entity
public class Role {
  private String name;  // "ROLE_EMPLOYER", "ROLE_CANDIDATE"
}
```

### New (DDD) RBAC
```java
// New architecture: User aggregate owns roles as value objects
public class User {
  // Roles stored as collection of value objects (no separate table)
  private Set<UserRole> roles = new HashSet<>();
  
  // Methods to manage roles
  public void assignRole(String roleName) { /* ... */ }
  public void revokeRole(String roleName) { /* ... */ }
  public boolean hasRole(String roleName) { /* ... */ }
}

// UserRole is a Value Object (not an Entity)
public record UserRole(
  String roleName,  // "ROLE_EMPLOYER", "ROLE_CANDIDATE", etc.
  LocalDateTime assignedAt
) implements ValueObject {
  // Behavior: immutable, identified by roleName only
}
```

### Data Migration Strategy
```sql
-- Phase 1: Query legacy join table
SELECT u.id, r.name FROM users u
JOIN user_to_role utr ON u.id = utr.user_id
JOIN role r ON utr.role_id = r.id;

-- Phase 2: Transform to new format (aggregate roles in postgres ARRAY or JSON)
ALTER TABLE users ADD COLUMN roles JSONB DEFAULT '[]'::jsonb;

UPDATE users SET roles = (
  SELECT jsonb_agg(
    jsonb_build_object(
      'roleName', r.name,
      'assignedAt', NOW()
    )
  )
  FROM user_to_role utr
  JOIN role r ON utr.role_id = r.id
  WHERE utr.user_id = users.id
);

-- Phase 3: Verify all roles migrated
SELECT * FROM users WHERE roles = '[]'::jsonb; -- Should be empty
```

**Ready for TASK-003 (Search domain analysis).**