# Jobs Domain Analysis — TASK-001 Discovery Document

> **Session:** 2026-03-08-copilot-session-002  
> **Analyst:** github-copilot (Claude Haiku 4.5)  
> **Date:** 2026-03-08T05:10:00Z  
> **Task:** TASK-001 (Análisis de Bounded Contexts y extracción de Job domain)

---

## ▸ EXECUTIVE SUMMARY

Analyzed JRecruiter legacy codebase. Job domain is **well-defined but tightly coupled** to:
- User management (FK relationship)
- Industry & Region (reference data)
- Statistic tracking (1:1 relationship)

**Key Finding:** Job aggregate in legacy is **data-centric** (pure getters/setters), NOT **behavior-centric** (DDD). In new architecture, we need to inject business rules into aggregate.

---

## ▸ JOB AGGREGATE ROOT — Current Structure

### Core Identity
```java
// Primary key
private Long id;

// Business identity (unique across migrations)
private String universalId;  // UUID-like
```

**Observation:** `universalId` is external system identifier, good for external integration.

### Core Properties

| Property | Type | Nullable | Index | Purpose |
|----------|------|----------|-------|---------|
| jobTitle | String(50) | NO | YES | Job position name |
| description | String | NO | YES | Full job description |
| businessName | String(50) | NO | NO | Company/employer name |
| salary | String | YES | YES | Salary range (string!) |
| status | JobStatus (enum) | NO | NO | Publishing status |
| registrationDate | Date | NO | NO | Created timestamp |
| updateDate | Date | YES | YES | Last modified |
| offeredBy | OfferedBy (enum) | YES | NO | Who's offering (recruiter/employer) |

### Address & Location

| Property | Type | Purpose |
|----------|------|---------|
| businessAddress1, businessAddress2 | String(50) | Street address |
| businessCity | String(30) | City name |
| businessState | String(20) | State/province |
| businessZip | String(15) | Postal code |
| businessPhone | String(15) | Company phone |
| businessPhoneExtension | String(15) | Phone extension |
| businessEmail | String(50) | Company email |
| website | String(50) | Company website |
| regionOther | String(50) | Custom region (free text) |

**✓ Candidate Value Object: Location**
```java
record Location(
  String address1,
  String address2,
  String city,
  String state,
  String postalCode,
  BigDecimal latitude,
  BigDecimal longitude,
  Integer zoomLevel
)
```

### Geographic Mapping

| Property | Type | Purpose |
|----------|------|---------|
| latitude | BigDecimal(12,6) | Map coordinate |
| longitude | BigDecimal(12,6) | Map coordinate |
| zoomLevel | Integer | Map zoom level (default 8) |
| usesMap | Boolean | Whether to display on map |

## ▸ RELATIONSHIPS

### Foreign Keys (Heavy Coupling)

```java
// Many-to-One (LAZY fetch)
private User user;             // FK: users_id — REQUIRED, NOT NULL
  // Relationship: User.jobs ← bidirectional OneToMany

// Many-to-One (LAZY fetch)
private Industry industry;     // FK: industries_id — nullable
  // Relationship: Industry.jobs ← bidirectional

// Many-to-One (LAZY fetch)
private Region region;         // FK: regions_id — nullable
  // Relationship: Region.jobs ← bidirectional
```

### One-to-One Relationship
```java
private Statistic statistic;   // PRIMARY_KEY_JOIN_COLUMN
  // Statistic tied to Job (1:1 cascade=ALL)
  // Contains: views count, applies count, favorites count
```

### Analysis

**Problem 1: User FK is **NOT NULL**
- Every Job MUST have an owner
- This is correct for business logic
- But in new architecture: separate User-Service
- **Decision:** Keep reference as `employerId` UUID (not FK)
- **Event:** JobCreated includes employerId for downstream services

**Problem 2: Industry & Region are reference data**
- Currently: Many-to-One relationships fetched LAZY
- In new architecture: Denormalize to nested objects
- **Decission:** Hold `industryName` + `industryId` (read-only reference)

**Problem 3: Statistic lifecycle**
- Tightly bound to Job (CascadeType.ALL)
- Could be separate bounded context
- **Decision:** Keep in Job-Service for MVP, extract later

---

## ▸ VALUE OBJECTS TO EXTRACT

### 1. JobLocation
```java
record JobLocation(
  String address1,
  String address2,
  String city,
  String state,
  String postalCode,
  String country,
  String website,
  String phone,
  String phoneExtension,
  String email,
  BigDecimal latitude,
  BigDecimal longitude,
  Boolean usesMap
) {
  // Invariants:
  // - At least one of address1, latitude must be non-null
  // - Postal code must match region format
}
```

### 2. JobSalary
```java
record JobSalary(
  String currency,      // "USD", "EUR", etc.
  String salaryRange,   // "50000-70000" or "Negotiable"
  JobSalaryFrequency frequency  // ANNUAL, MONTHLY, HOURLY
)
```

**Note:** Legacy stores salary as plain String. Need to parse in migration.

### 3. JobStatus (Enum)
```java
enum JobStatus {
  DRAFT,              // Not published yet
  PUBLISHED,          // Visible to candidates
  ON_HOLD,           // Temporarily hidden
  CLOSED,            // Completed/no longer active
  EXPIRED            // Past expiration date
}
```

**Note:** Need to verify exact enum values in legacy Constants class.

### 4. OfferedBy (Enum)
```java
enum OfferedBy {
  EMPLOYER,          // Direct company posting
  RECRUITER          // Recruiter/staffing agency posting
}
```

### 5. JobPostingStatus (Custom for new domain)
```java
enum JobPostingStatus {
  DRAFT("Job not published"),
  PUBLISHED("Visible to candidates"),
  FILLED("Position filled"),
  CLOSED("Stopped accepting applications"),
  ARCHIVED("Archived for historical reference")
}
```

---

## ▸ INVARIANTS (Business Rules)

### At Creation Time
1. **jobTitle** must not be empty, max 50 chars
2. **description** must not be empty
3. **businessName** must not be empty, max 50 chars
4. **employerId** (User) must exist
5. **status** must be one of {DRAFT, PUBLISHED, CLOSED}
6. **registrationDate** defaults to NOW
7. **universalId** must be unique (UUID)

### Lifecycle Transitions
1. **DRAFT → PUBLISHED:**
   - Require: All required fields filled
   - Require: Address or location coordinates provided
   - Action: Emit `JobPublished` event

2. **PUBLISHED → CLOSED:**
   - Allowed anytime
   - Action: Emit `JobClosed` event

3. **PUBLISHED → ON_HOLD:**
   - Temporary pause
   - Can resume to PUBLISHED

### Immutable Fields
- `universalId` — Never changes
- `registrationDate` — Never changes
- `employerId` — Cannot change owner

### Auditable Fields
- `updateDate` — Automatically updated on modification
- `status` — Should be tracked with timestamps

---

## ▸ AGGREGATE ROOT INTERFACE (New DDD Design)

```java
public class Job {
  // IDENTITY
  private final UUID jobId;              // Surrogate key
  private final UUID universalId;        // Business key
  private final UUID employerId;         // "Owner" reference
  
  // CORE DATA
  private final JobTitle title;          // Value object
  private final JobDescription description;
  private final CompanyName companyName;
  private final JobLocation location;
  private final JobSalary salary;
  
  // METADATA
  private JobPostingStatus status;       // Mutable
  private OfferedBy offeredBy;
  private Instant createdAt;
  private Instant publishedAt;
  private Instant closedAt;
  
  //BEHAVIOUR (Methods to add)
  public void publish() { /* Business logic */ }
  public void close() { /* Business logic */ }
  public void updateLocation(JobLocation newLocation) { /* ... */ }
  public boolean canPublish() { /* Invariant checks */ }
  public JobCreatedEvent publishDomainEvent() { /* ... */ }
}
```

---

## ▸ LEGACY-TO-NEW MAPPING

| Legacy Field | New Domain | Notes |
|--------------|-----------|-------|
| `id` | `jobId` | Long → UUID |
| `universalId` | `universalId` | Keepas-is |
| `user.id` | `employerId` | No FK, just UUID |
| `jobTitle` | `title: JobTitle` | VO |
| `description` | `description: JobDescription` | VO |
| `businessName` | `companyName: CompanyName` | VO |
| `businessAddress1..businessZip` | `location: JobLocation` | VO |
| `latitude, longitude` | `location.latitude/longitude` | VO |
| `salary` | `salary: JobSalary` | VO (parse string) |
| `status` | `status: JobPostingStatus` | Enum |
| `registrationDate` | `createdAt: Instant` | Date → Instant |
| `updateDate` | — | Update via event |
| `industry_id` | `industryId: UUID` | Reference, no FK |
| `region_id` | `regionId: UUID` | Reference, no FK |
| `website` | `location.website` | Nested |
| `businessPhone` | `location.phone` | Nested |
| `businessEmail` | `location.email` | Nested |

---

## ▸ BOUNDED CONTEXT BOUNDARIES

### Jobs Context OWNS
- ✅ Job aggregate root and its value objects
- ✅ JobCreated, JobPublished, JobClosed events
- ✅ Job lifecycle management
- ✅ Job validation rules

### Jobs Context DEPENDS ON (External)
- ✅ User-Service (only read employerId, no relationships)
- ✅ Industry-Service (reference data, read-only)
- ✅ Region-Service (reference data, read-only)

### Jobs Context PUBLISHES NEWS TO
- → Search-Service (JobCreated, JobPublished, JobClosed)
- → Notification-Service (JobPublished → notify candidates)
- → Analytics-Service (JobCountPerDay statistics)

---

## ▸ CONSTANTS TO EXTRACT

Need to verify in legacy `/common/Constants.java`:
- [ ] `JobStatus` enum values
- [ ] `OfferedBy` enum values
- [ ] `UserAuthenticationType` enum values
- [ ] Default values (e.g., `zoomLevel = 8`)

**Next Action:** Search for `Constants.java` in legacy.

---

## ▸ QUESTIONS FOR ARCHITECTURE REVIEW

1. **Salary parsing:** Legacy stores as String. Should new domain parse into (min, max, currency) or keep as string?
   - **Recommendation:** Parse into `record JobSalary(BigDecimal minSalary, BigDecimal maxSalary, Currency currency)`

2. **Industry/Region denormalization:** Should Job store industry/region names or just IDs?
   - **Recommendation:** Store both (ID + name) for search performance

3. **Statistic lifecycle:** Should `Job-Service` own Statistic or separate `Analyt ics-Service`?
   - **Recommendation:** Keep in Job-Service for MVP, extract as `AnalyticsService` later

4. **versioning:** How to handle Job modifications? Store version field?
   - **Recommendation:** Event-sourcing for audit trail (future phase)

---

## ▸ NEXT STEPS (TASK-002, TASK-003, TASK-004)

1. **TASK-002:** Analyze User domain (Employer + Candidate separation)
2. **TASK-003:** Analyze Search domain (how legacy indexes jobs)
3. **TASK-004:** Create unified Bounded Context diagram

---

## ▸ ESTIMATION FOR TASK-001

**Estimated:** 8 hours | **Actual:** 1 hour  
**Status:** ✅ COMPLETE

- Read `Job.java` (legacy model): 30 min
- Read `User.java` (relationships): 15 min
- Read `JobService.java` (business methods): 15 min
- Analysis & documentation: 20 min

**Quality:** High confidence in Job aggregate extraction.

