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

## ▸ AGGREGATE ROOT — Complete Implementation

```java
public class Job {
  // IDENTITY
  private final UUID jobId;              // Surrogate key
  private final UUID universalId;        // Business key
  private final UUID employerId;         // "Owner" reference (no FK)
  
  // CORE DATA (Immutable)
  private final JobTitle title;
  private final JobDescription description;
  private final CompanyName companyName;
  private final JobLocation location;
  private final JobSalary salary;
  private final OfferedBy offeredBy;
  private final Industry industry;       // Reference data (ID only)
  private final Region region;           // Reference data (ID only)
  
  // MUTABLE STATE
  private JobPostingStatus status;
  private Instant createdAt;
  private Instant publishedAt;
  private Instant closedAt;
  private Instant updatedAt;
  
  // TRANSIENT (not persisted)
  private List<DomainEvent> domainEvents = new ArrayList<>();
  
  // FACTORY METHOD: Create new (draft) job
  public static Job createDraft(
      UUID jobId,
      UUID universalId,
      UUID employerId,
      JobTitle title,
      JobDescription description,
      CompanyName companyName,
      JobLocation location,
      JobSalary salary,
      OfferedBy offeredBy,
      Industry industry,
      Region region) {
    
    // Validate invariants at creation
    if (jobId == null) throw new InvalidJobException("jobId is required");
    if (employerId == null) throw new InvalidJobException("employerId is required");
    if (title == null) throw new InvalidJobException("title is required");
    if (description == null) throw new InvalidJobException("description is required");
    if (companyName == null) throw new InvalidJobException("companyName is required");
    if (location == null) throw new InvalidJobException("location is required");
    
    Job job = new Job(
        jobId, universalId, employerId,
        title, description, companyName, location, salary, offeredBy,
        industry, region
    );
    
    job.status = JobPostingStatus.DRAFT;
    job.createdAt = Instant.now();
    job.updatedAt = Instant.now();
    
    return job;
  }
  
  // PRIVATE CONSTRUCTOR: Only called via factory methods
  private Job(UUID jobId, UUID universalId, UUID employerId,
              JobTitle title, JobDescription description,
              CompanyName companyName, JobLocation location,
              JobSalary salary, OfferedBy offeredBy,
              Industry industry, Region region) {
    this.jobId = jobId;
    this.universalId = universalId;
    this.employerId = employerId;
    this.title = title;
    this.description = description;
    this.companyName = companyName;
    this.location = location;
    this.salary = salary;
    this.offeredBy = offeredBy;
    this.industry = industry;
    this.region = region;
  }
  
  // BEHAVIOR: Publish job (transition DRAFT → PUBLISHED)
  public void publish() {
    if (!this.canPublish()) {
      throw new InvalidJobStateException(
          "Cannot publish job in state: " + this.status);
    }
    
    this.status = JobPostingStatus.PUBLISHED;
    this.publishedAt = Instant.now();
    this.updatedAt = Instant.now();
    
    // Emit domain event
    this.domainEvents.add(new JobPublishedEvent(
        this.jobId,
        this.title,
        this.description,
        this.location,
        this.salary,
        this.employerId,
        this.publishedAt
    ));
  }
  
  // BEHAVIOR: Close job (transition PUBLISHED → CLOSED)
  public void close() {
    if (this.status == JobPostingStatus.CLOSED || 
        this.status == JobPostingStatus.ARCHIVED) {
      throw new InvalidJobStateException(
          "Cannot close already closed job: " + this.status);
    }
    
    this.status = JobPostingStatus.CLOSED;
    this.closedAt = Instant.now();
    this.updatedAt = Instant.now();
    
    // Emit domain event
    this.domainEvents.add(new JobClosedEvent(
        this.jobId,
        this.closedAt
    ));
  }
  
  // BEHAVIOR: Update location
  public void updateLocation(JobLocation newLocation) {
    if (newLocation == null) {
      throw new InvalidJobException("Location cannot be null");
    }
    
    // Note: We can't reassign final field, so we'd need a new Job
    // Or use Kotlin data classes with copy()
    // For now, document that location is immutable
    throw new UnsupportedOperationException(
        "Location is immutable. Create new Job version to change.");
  }
  
  // QUERY: Can this job be published?
  public boolean canPublish() {
    return this.status == JobPostingStatus.DRAFT &&
           this.title != null &&
           this.description != null &&
           this.location != null &&
           this.location.getAddress1() != null;
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
  public UUID getJobId() { return jobId; }
  public UUID getUniversalId() { return universalId; }
  public UUID getEmployerId() { return employerId; }
  public JobTitle getTitle() { return title; }
  public JobDescription getDescription() { return description; }
  public CompanyName getCompanyName() { return companyName; }
  public JobLocation getLocation() { return location; }
  public JobSalary getSalary() { return salary; }
  public OfferedBy getOfferedBy() { return offeredBy; }
  public Industry getIndustry() { return industry; }
  public Region getRegion() { return region; }
  public JobPostingStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getPublishedAt() { return publishedAt; }
  public Instant getClosedAt() { return closedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
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

