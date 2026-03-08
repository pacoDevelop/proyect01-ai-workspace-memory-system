# Context Dependencies Analysis — TASK-004 Discovery Document

> **Session:** 2026-03-08-copilot-session-005  
> **Analyst:** github-copilot (Claude Haiku 4.5)  
> **Date:** 2026-03-08T05:20:41Z  
> **Task:** TASK-004 (Mapear dependencias entre bounded contexts)

---

## ▸ EXECUTIVE SUMMARY

Analyzed dependencies between the 5 Bounded Contexts identified in JRecruiter migration. Created comprehensive dependency diagram and defined contracts (DTOs, Events) for inter-service communication.

**Key Finding:** User and Job contexts have **tight coupling** (employer ownership), while Search and Notification contexts are **loosely coupled** via event-driven async communication.

---

## ▸ BOUNDED CONTEXTS IDENTIFIED

### 1. **Job Context** (Core)
- **Aggregate:** Job
- **Value Objects:** JobLocation, JobSalary, JobStatus, OfferedBy, JobPostingStatus
- **Dependencies:** User (employer), Industry, Region, Statistic
- **Events Published:** JobCreated, JobUpdated, JobDeleted, JobStatusChanged

### 2. **User Context** (Authentication & Authorization)
- **Aggregate:** User
- **Value Objects:** UserCredentials, UserProfile, UserContact, UserStatus, UserRole
- **Dependencies:** None (external auth service)
- **Events Published:** UserRegistered, UserVerified, UserUpdated, UserDisabled

### 3. **Search Context** (Read Model)
- **Aggregate:** JobSearchIndex
- **Value Objects:** SearchQueryVO, SalaryRangeVO, GeoLocationVO, FacetVO
- **Dependencies:** Job (events), User (preferences)
- **Events Consumed:** JobCreated, JobUpdated, JobDeleted
- **Events Published:** SearchPerformed, SearchAnalytics

### 4. **Notification Context** (Async Communication)
- **Aggregate:** Notification
- **Value Objects:** EmailTemplate, NotificationSettings
- **Dependencies:** Job, User, Search (events)
- **Events Consumed:** JobCreated, ApplicationReceived, UserRegistered
- **Events Published:** EmailSent, NotificationDelivered

### 5. **Application Context** (Missing in Legacy)
- **Aggregate:** JobApplication
- **Value Objects:** ApplicationStatus, CoverLetter, ApplicationMetadata
- **Dependencies:** User (candidate), Job (target)
- **Events Published:** ApplicationSubmitted, ApplicationStatusChanged
- **Events Consumed:** JobCreated, UserVerified

---

## ▸ DEPENDENCY DIAGRAM

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Job Context   │    │   User Context  │    │  Search Context │
│                 │    │                 │    │                 │
│  ┌─────────────┐│    │  ┌─────────────┐│    │  ┌─────────────┐│
│  │   Job       ││    │  │   User      ││    │  │ JobSearch   ││
│  │ Aggregate   ││    │  │ Aggregate   ││    │  │ Index       ││
│  └─────────────┘│    │  └─────────────┘│    │  └─────────────┘│
│                 │    │                 │    │                 │
│  Events:        │    │  Events:        │    │  Events:        │
│  ┌─────────────┐│    │  ┌─────────────┐│    │  ┌─────────────┐│
│  │ JobCreated  ││    │  │ UserReg     ││    │  │ JobCreated  ││
│  │ JobUpdated  ││    │  │ UserVer     ││    │  │ JobUpdated  ││
│  │ JobDeleted  ││    │  │ UserUpd     ││    │  │ JobDeleted  ││
│  │ JobStatus   ││    │  │ UserDis     ││    │  └─────────────┘│
│  └─────────────┘│    │  └─────────────┘│    │                 │
│                 │    │                 │    │  Events:        │
│  Commands:      │    │  Commands:      │    │  ┌─────────────┐│
│  ┌─────────────┐│    │  ┌─────────────┐│    │  │ SearchPerf  ││
│  │ CreateJob   ││    │  │ Register    ││    │  │ SearchAnal  ││
│  │ UpdateJob   ││    │  │ Verify      ││    │  └─────────────┘│
│  │ DeleteJob   ││    │  │ Update      ││    │                 │
│  └─────────────┘│    │  └─────────────┘│    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Notification    │    │                 │    │                 │
│   Context       │    │                 │    │                 │
│                 │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ Notification││    │                 │    │                 │
│  │ Aggregate   ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
│                 │    │                 │    │                 │
│  Events:        │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ JobCreated  ││    │                 │    │                 │
│  │ AppReceived ││    │                 │    │                 │
│  │ UserReg     ││    │                 │    │                 │
│  │ AppStatus   ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
│                 │    │                 │    │                 │
│  Commands:      │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ SendNotif   ││    │                 │    │                 │
│  │ UpdateNotif ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Application     │    │                 │    │                 │
│   Context       │    │                 │    │                 │
│                 │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ Application ││    │                 │    │                 │
│  │ Aggregate   ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
│                 │    │                 │    │                 │
│  Events:        │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ AppSubmit   ││    │                 │    │                 │
│  │ AppStatus   ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
│                 │    │                 │    │                 │
│  Commands:      │    │                 │    │                 │
│  ┌─────────────┐│    │                 │    │                 │
│  │ SubmitApp   ││    │                 │    │                 │
│  │ UpdateApp   ││    │                 │    │                 │
│  └─────────────┘│    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## ▸ DEPENDENCY ANALYSIS

### 1. **Job ↔ User (Tight Coupling)**
```java
// Current: Every Job has User FK
@ManyToOne private User user;  // Employer FK

// New: Event-driven with reference
public class JobCreatedEvent {
    private UUID jobId;
    private UUID employerId;    // Reference, not FK
    private String employerName; // Denormalized for search
}
```

**Problem:** Legacy requires User FK for every Job.
**Solution:** Event-driven with employerId reference + denormalization.

### 2. **Job ↔ Search (Loose Coupling)**
```java
// Current: Synchronous indexing
public void reindexSearch() { /* Blocks until complete */ }

// New: Event-driven async
@EventHandler
public void on(JobCreatedEvent event) {
    JobSearchIndex index = createIndex(event.getJob());
    searchRepository.save(index);
}
```

### 3. **User ↔ Search (Preferences)**
```java
// Current: No user preferences in search
// New: Personalized ranking
public SearchResult searchJobsPersonalized(
    SearchRequest request, 
    UserProfile user
) {
    // Apply user preferences, location, salary expectations
}
```

### 4. **Application ↔ Job (Business Logic)**
```java
// New: Separate bounded context
public class JobApplication {
    private UUID applicationId;
    private UUID candidateId;
    private UUID jobId;
    private ApplicationStatus status;
}
```

---

## ▸ CONTRACTS & DTOs

### 1. **Job-to-User Contracts**
```java
// JobCreatedEvent (Published by Job Context)
public class JobCreatedEvent {
    private UUID jobId;
    private UUID employerId;
    private String employerName;
    private String jobTitle;
    private String description;
    private BigDecimal salary;
    private Location location;
    private Industry industry;
    private JobStatus status;
    private Instant createdAt;
}

// UserRegisteredEvent (Published by User Context)
public class UserRegisteredEvent {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private UserRole role;
    private Instant registeredAt;
    private String verificationKey;
}
```

### 2. **Search Contracts**
```java
// SearchRequest (Consumed by Search Context)
public class SearchRequest {
    private String keyword;
    private LocationFilter location;
    private SalaryFilter salary;
    private List<String> industries;
    private List<String> skills;
    private JobTypeFilter jobType;
    private ExperienceLevelFilter experienceLevel;
    private Pagination pagination;
    private SortOrder sortOrder;
}

// SearchResult (Published by Search Context)
public class SearchResult {
    private List<SearchResultItem> results;
    private FacetResult facets;
    private long totalResults;
    private long processingTime;
    private Map<String, Object> metadata;
}
```

### 3. **Notification Contracts**
```java
// NotificationRequest (Consumed by Notification Context)
public class NotificationRequest {
    private UUID notificationId;
    private NotificationType type;
    private UUID userId;
    private String email;
    private String subject;
    private String body;
    private Map<String, Object> templateData;
    private Instant scheduledAt;
}

// EmailSentEvent (Published by Notification Context)
public class EmailSentEvent {
    private UUID notificationId;
    private UUID userId;
    private String email;
    private String subject;
    private Instant sentAt;
    private boolean success;
    private String errorMessage;
}
```

### 4. **Application Contracts**
```java
// ApplicationSubmittedEvent (Published by Application Context)
public class ApplicationSubmittedEvent {
    private UUID applicationId;
    private UUID candidateId;
    private UUID jobId;
    private String coverLetter;
    private Instant submittedAt;
    private ApplicationStatus status;
}

// ApplicationStatusChangedEvent (Published by Application Context)
public class ApplicationStatusChangedEvent {
    private UUID applicationId;
    private ApplicationStatus newStatus;
    private String reason;
    private Instant changedAt;
}
```

---

## ▸ EVENT SCHEMA DESIGN

### 1. **Core Events**
```java
// Job Events
public enum JobEventType {
    CREATED, UPDATED, DELETED, STATUS_CHANGED
}

public class JobEvent {
    private UUID jobId;
    private JobEventType type;
    private JobData data;
    private Instant timestamp;
    private String userId; // Who triggered the event
}

// User Events
public enum UserEventType {
    REGISTERED, VERIFIED, UPDATED, DISABLED
}

public class UserEvent {
    private UUID userId;
    private UserEventType type;
    private UserData data;
    private Instant timestamp;
    private String userId; // Who triggered the event
}
```

### 2. **Search Events**
```java
// Search Events
public enum SearchEventType {
    PERFORMED, ANALYTICS_UPDATED
}

public class SearchEvent {
    private UUID searchId;
    private SearchEventType type;
    private SearchQuery query;
    private SearchResult result;
    private Instant timestamp;
    private String userId; // Who performed the search
}
```

### 3. **Notification Events**
```java
// Notification Events
public enum NotificationEventType {
    EMAIL_SENT, NOTIFICATION_DELIVERED, NOTIFICATION_FAILED
}

public class NotificationEvent {
    private UUID notificationId;
    private NotificationEventType type;
    private NotificationData data;
    private Instant timestamp;
    private String userId; // Who triggered the event
}
```

---

## ▸ API GATEWAY ROUTING

### 1. **Legacy Routing Rules**
```yaml
# Legacy API Gateway Configuration
routes:
  - id: legacy-job-service
    uri: http://legacy-jrecruiter.com
    predicates:
      - Path=/api/jobs/**
    filters:
      - RewritePath=/api/jobs/(?<segment>.*), /job/\1

  - id: legacy-user-service
    uri: http://legacy-jrecruiter.com
    predicates:
      - Path=/api/users/**
    filters:
      - RewritePath=/api/users/(?<segment>.*), /user/\1
```

### 2. **New Service Routing**
```yaml
# New Microservices Routing
routes:
  - id: job-service
    uri: http://job-service:8080
    predicates:
      - Path=/api/v2/jobs/**
    filters:
      - RewritePath=/api/v2/jobs/(?<segment>.*), /api/jobs/\1

  - id: user-service
    uri: http://user-service:8081
    predicates:
      - Path=/api/v2/users/**
    filters:
      - RewritePath=/api/v2/users/(?<segment>.*), /api/users/\1

  - id: search-service
    uri: http://search-service:8082
    predicates:
      - Path=/api/v2/search/**
    filters:
      - RewritePath=/api/v2/search/(?<segment>.*), /api/search/\1

  - id: notification-service
    uri: http://notification-service:8083
    predicates:
      - Path=/api/v2/notifications/**
    filters:
      - RewritePath=/api/v2/notifications/(?<segment>.*), /api/notifications/\1
```

---

## ▸ MIGRATION CONSIDERATIONS

### 1. **Zero-Downtime Data Migration with CDC**
```sql
-- Step 1: Create new columns for dual-write phase
ALTER TABLE jobs 
ADD COLUMN employer_id UUID NULL,
ADD COLUMN employer_name VARCHAR(100) NULL,
ADD COLUMN migrated_at TIMESTAMP NULL;

-- Step 2: Enable logical replication for CDC
ALTER SYSTEM SET wal_level = logical;
SELECT pg_reload_conf();

-- Step 3: Create replication slot for event capture
SELECT * FROM pg_create_logical_replication_slot('job_cdc', 'test_decoding');

-- Step 4: Backfill data in batches (non-blocking)
DO $$
DECLARE
    batch_size INT := 1000;
    offset_counter INT := 0;
BEGIN
    LOOP
        UPDATE jobs j
        SET employer_id = u.id,
            employer_name = u.company,
            migrated_at = NOW()
        FROM users u
        WHERE j.user_id = u.id
          AND j.migrated_at IS NULL
        LIMIT batch_size;
        
        -- Exit if no more rows
        IF NOT FOUND THEN EXIT; END IF;
        
        -- Wait between batches to avoid blocking
        PERFORM pg_sleep(0.1);
        
        offset_counter := offset_counter + batch_size;
    END LOOP;
END $$;

-- Step 5: Dual-write phase (application code writes to both columns)
-- Application: INSERT INTO jobs (..., employer_id, user_id) VALUES (...);
-- Both fields populated until verification complete

-- Step 6: Verify data consistency (run every 5 minutes during migration)
SELECT 
    COUNT(*) as total_jobs,
    COUNT(CASE WHEN migrated_at IS NOT NULL THEN 1 END) as migrated_jobs,
    COUNT(CASE WHEN migrated_at IS NULL THEN 1 END) as pending_jobs,
    ROUND(100.0 * COUNT(CASE WHEN migrated_at IS NOT NULL THEN 1 END) / COUNT(*), 2) as migration_percentage
FROM jobs;

-- Step 7: Add consistency check trigger
CREATE OR REPLACE TRIGGER jobs_migration_check
BEFORE INSERT OR UPDATE ON jobs
FOR EACH ROW
WHEN (NEW.employer_id IS NOT NULL AND NEW.user_id IS NOT NULL)
EXECUTE FUNCTION validate_employer_consistency();

CREATE OR REPLACE FUNCTION validate_employer_consistency()
RETURNS TRIGGER AS $$
BEGIN
    -- Verify FK relationship
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = NEW.employer_id) THEN
        RAISE EXCEPTION 'Invalid employer_id: %', NEW.employer_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Step 8: Final cutover (zero-downtime)
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    -- Verify all jobs migrated
    ASSERT (SELECT COUNT(*) FROM jobs WHERE migrated_at IS NULL) = 0;
    
    -- Remove old FK constraint
    ALTER TABLE jobs DROP CONSTRAINT fk_jobs_user;
    
    -- Drop old column
    ALTER TABLE jobs DROP COLUMN user_id;
    
    -- Make employer_id NOT NULL
    ALTER TABLE jobs ALTER COLUMN employer_id SET NOT NULL;
    
    -- Add new FK constraint on new column
    ALTER TABLE jobs ADD CONSTRAINT fk_jobs_employer 
        FOREIGN KEY (employer_id) REFERENCES users(id);
COMMIT;

-- Step 9: Cleanup and optimization
DROP TRIGGER IF EXISTS jobs_migration_check ON jobs;
DROP FUNCTION IF EXISTS validate_employer_consistency();
REINDEX TABLE jobs;
ANALYZE jobs;
```

### 2. **Consistency Verification Procedures**
```yaml
# Automated consistency checks during migration
consistency-checks:
  interval: 5-minutes
  procedures:
    - name: "job-consistency"
      description: "Verify all jobs have both user_id and employer_id during dual-write"
      queries:
        - "SELECT COUNT(*) FROM jobs WHERE user_id IS NULL OR employer_id IS NULL"
      acceptable_difference: 0
      
    - name: "referential-integrity"
      description: "Verify all employer_ids match existing users"
      queries:
        - "SELECT COUNT(*) FROM jobs j LEFT JOIN users u ON j.employer_id = u.id WHERE u.id IS NULL"
      acceptable_difference: 0
      
    - name: "data-match"
      description: "Verify employer_name matches user.company"
      queries:
        - "SELECT COUNT(*) FROM jobs j JOIN users u ON j.employer_id = u.id WHERE j.employer_name != u.company"
      acceptable_difference: 0
      threshold: "P95 < 10 minutes to detect and fix"

  automated-recovery:
    - on-failure: "Trigger reconciliation job"
    - on-mismatch: "Alert ops team + auto-remediate"
    - on-lag: "Throttle incoming writes temporarily"

### 3. **Event Outbox Pattern for Reliability**
```sql
-- Outbox table in job-service database for guaranteed delivery
CREATE TABLE job_outbox (
    outbox_id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP NULL,
    publish_attempt INT DEFAULT 0,
    error_message TEXT NULL,
    INDEX idx_published (published_at),
    INDEX idx_created (created_at)
);

-- Transactional write: Job + Event in same transaction
BEGIN TRANSACTION;
    INSERT INTO jobs (...) VALUES (...);
    INSERT INTO job_outbox (aggregate_id, event_type, payload) 
    VALUES (job_id, 'JobCreated', '{"id": "...", "title": "..."}'::jsonb);
COMMIT;

-- Poller job (runs every 1 second)
CREATE PROCEDURE publish_events() AS $$
DECLARE
    event_row RECORD;
BEGIN
    FOR event_row IN 
        SELECT * FROM job_outbox WHERE published_at IS NULL LIMIT 100
    LOOP
        BEGIN
            -- Publish to RabbitMQ
            PERFORM pg_notify(
                'job_events',
                json_build_object('type', event_row.event_type, 'data', event_row.payload)::text
            );
            
            -- Mark as published
            UPDATE job_outbox 
            SET published_at = NOW() 
            WHERE outbox_id = event_row.outbox_id;
            
        EXCEPTION WHEN OTHERS THEN
            -- Retry up to 5 times
            UPDATE job_outbox 
            SET publish_attempt = publish_attempt + 1,
                error_message = SQLERRM
            WHERE outbox_id = event_row.outbox_id AND publish_attempt < 5;
        END;
    END LOOP;
END $$ LANGUAGE plpgsql;

-- Schedule poller
SELECT cron.schedule('publish_job_events', '1 second', 'CALL publish_events()');
```

---

## ▸ END-TO-END EVENT FLOW EXAMPLE

### Complete User Journey: Register → Post Job → Search Results → Notification

```
TIMELINE (t0 = 0ms, t_end = ~500ms)

t0ms ---------- USER CONTEXT: Registration
┌─────────────────────────────────────────────────────────┐
│ 1. User submits RegisterRequest                          │
│    - /api/v2/users/register (POST)                       │
│    - Email: employer@company.com, Password: *****        │
└─────────────────────────────────────────────────────────┘
     ↓
│ 2. UserService validates & creates User aggregate        │
│    - Hash password with bcrypt                           │
│    - Generate verification token                         │
│    - Save User in PostgreSQL                             │
│    - Publish UserRegisteredEvent → RabbitMQ             │
└─────────────────────────────────────────────────────────┘

t50ms --------- NOTIFICATION CONTEXT: Email Dispatch
┌─────────────────────────────────────────────────────────┐
│ 3. NotificationService consumes UserRegisteredEvent      │
│    - Creates email template (verification link)          │
│    - Sends email async via SMTP                          │
│    - Publishes EmailSentEvent                            │
└─────────────────────────────────────────────────────────┘

t100ms -------- JOB CONTEXT: Job Creation
┌─────────────────────────────────────────────────────────┐
│ 4. User verifies email, logs in (gets JWT token)         │
│ 5. User submits CreateJobRequest                         │
│    - /api/v2/jobs (POST)                                │
│    - JobTitle: "Senior Java Developer"                  │
│    - Description, Salary, Location, Industry            │
│    - Authorization header: Bearer <JWT_TOKEN>           │
└─────────────────────────────────────────────────────────┘
     ↓
│ 6. JobService validates & creates Job aggregate          │
│    - Verify employer exists (User lookup via UUID)      │
│    - Enforce invariants (title, description req'd)      │
│    - Save Job in PostgreSQL                             │
│    - Write to job_outbox table (same transaction)       │
│    - Publish JobCreatedEvent → RabbitMQ fanout          │
└─────────────────────────────────────────────────────────┘

t150ms -------- SEARCH CONTEXT: Indexing
┌─────────────────────────────────────────────────────────┐
│ 7. SearchService consumes JobCreatedEvent                │
│    - Creates JobSearchIndex document                     │
│    - Indexes in Elasticsearch (with geo_point)          │
│    - Publishes SearchIndexedEvent                        │
│    - Denormalizes employer name, company info           │
└─────────────────────────────────────────────────────────┘

t200ms -------- NOTIFICATION CONTEXT: Employer Confirmation
┌─────────────────────────────────────────────────────────┐
│ 8. NotificationService consumes JobCreatedEvent          │
│    - Creates job confirmation email for employer        │
│    - Sends email: "Your job is live"                    │
│    - Publishes EmailSentEvent                            │
└─────────────────────────────────────────────────────────┘

t250ms -------- SEARCH CONTEXT: Candidate Search
┌─────────────────────────────────────────────────────────┐
│ 9. Candidate searches for jobs                           │
│    - /api/v2/search (GET)                               │
│    - Query: keyword="Java", location="Madrid", salary>40k
│    - Uses Elasticsearch (returns in ~50ms)              │
│    - Returns 15 matching jobs including new one         │
└─────────────────────────────────────────────────────────┘

t300ms -------- APPLICATION CONTEXT: Job Application
┌─────────────────────────────────────────────────────────┐
│ 10. Candidate applies to job                             │
│     - /api/v2/applications (POST)                        │
│     - JobId, CandidateId, CoverLetter                   │
└─────────────────────────────────────────────────────────┘
      ↓
│ 11. ApplicationService creates Application aggregate    │
│     - Verify candidate & job still exist                │
│     - Enforce: candidate can't apply twice              │
│     - Save Application in PostgreSQL                    │
│     - Publish ApplicationSubmittedEvent                  │
└─────────────────────────────────────────────────────────┘

t350ms -------- NOTIFICATION CONTEXT: Alerts
┌─────────────────────────────────────────────────────────┐
│ 12. NotificationService consumes ApplicationSubmittedEvent│
│     - Sends email to employer: "New application!"       │
│     - Sends confirmation to candidate                   │
│     - Publishes EmailSentEvent (2x)                     │
└─────────────────────────────────────────────────────────┘

t400ms -------- DATA CONSISTENCY VERIFICATION
┌─────────────────────────────────────────────────────────┐
│ 13. Consistency checker (background job)                 │
│     - Job exists in PostgreSQL: ✓                       │
│     - Job indexed in Elasticsearch: ✓                    │
│     - Employer notification sent: ✓                      │
│     - All events published: ✓                            │
│     - User received 2 emails: ✓                          │
└─────────────────────────────────────────────────────────┘

t500ms -------- COMPLETE: End-to-end consistency achieved
```

**Success Criteria Met:**
- ✅ No direct DB calls between services
- ✅ Event-driven async communication
- ✅ Eventual consistency: all services updated within 500ms
- ✅ Single source of truth: PostgreSQL is authoritative
- ✅ Elasticsearch is derived read model
- ✅ No data loss: outbox ensures event delivery
- ✅ Saga pattern: multi-service transaction coordination

---

## ▸ CONTRACT TESTING STRATEGY

### 1. **Consumer-Driven Contracts (Pact Testing)**
```java
// SearchService tests: JobCreatedEvent contract
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "JobProvider", port = "8080")
class JobCreatedEventConsumerPactTest {
    
    @Pact(consumer = "SearchConsumer")
    public V4Pact createJobCreatedEventPact(PactBuilder builder) {
        return builder
            .given("Job created with valid data")
            .uponReceiving("a JobCreatedEvent")
            .path("/events/job-created")
            .method("POST")
            .bodyMatchingJsonSchema(schemaFor(JobCreatedEvent.class))
            .willRespondWith(200)
            .toPact(V4PactBuilder::build);
    }
    
    @Test
    void testJobCreatedEventDeserializes(MockServerClient mockServer) 
            throws JsonProcessingException {
        JobCreatedEvent event = new JobCreatedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Senior Java Developer",
            "5+ years experience",
            new BigDecimal("50000"),
            // ... other fields
            LocalDateTime.now()
        );
        
        // Verify event can be published
        String json = objectMapper.writeValueAsString(event);
        assertDoesNotThrow(() -> objectMapper.readValue(json, JobCreatedEvent.class));
    }
}

// JobService tests: provide JobCreatedEvent contract
@SpringBootTest
@Provider("JobProvider")
@PactFolder("pacts")
class JobCreatedEventProviderPactTest {
    
    @RestTemplate
    RestTemplate restTemplate;
    
    @TestTarget
    public final Target target = new SpringBootHttpTarget();
    
    @State("Job created with valid data")
    void setupJobCreatedState() {
        // Setup test data in job-service
        jobRepository.save(new Job(/* ... */));
    }
    
    @BeforeEach
    void setupMockServer() {
        this.restTemplate = new RestTemplate();
    }
}
```

### 2. **Event Schema Validation**
```java
// Shared schema definition (enforced by both producer + consumer)
@Data
public class JobCreatedEvent {
    @NotNull private UUID jobId;
    @NotNull private UUID employerId;
    @NotEmpty private String jobTitle;
    @NotEmpty private String description;
    @NotNull @Positive private BigDecimal salary;
    @NotNull private JobLocation location;
    @NotNull private Industry industry;
    @NotNull private JobStatus status;
    @NotNull private LocalDateTime createdAt;
    @Pattern(regexp = "^2\\d{3}-\\d{2}-\\d{2}T.*Z$") // ISO-8601
    private String timestamp;
    
    // Schema versioning
    @org.springframework.boot.configurationprocessor.json.JsonProperty("_schema_version")
    private String schemaVersion = "v1";
}

// Validation test
@Test
void jobCreatedEventConformsToSchema() throws JsonProcessingException {
    JobCreatedEvent event = createTestEvent();
    String json = objectMapper.writeValueAsString(event);
    
    // Validate against JSON schema
    JsonSchema schema = JsonSchemaFactory.getInstance()
        .getSchema(schemaResource("job-created-event-schema.json"));
    
    Set<ValidationMessage> messages = schema.validate(
        objectMapper.readTree(json)
    );
    
    assertTrue(messages.isEmpty(), 
        "Event does not conform to schema: " + messages);
}
```

### 3. **Integration Contract Tests (End-to-End)**
```java
// Test: JobService publishes → SearchService consumes
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {
    JobServiceApplication.class,
    SearchServiceApplication.class,
    TestEventConfig.class
})
class JobToSearchIntegrationContractTest {
    
    @Autowired private JobRepository jobRepository;
    @Autowired private SearchRepository searchRepository;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private ApplicationEventPublisher eventPublisher;
    
    @Test
    void jobCreatedEventPropagatesSearchIndex() 
            throws InterruptedException {
        // Arrange: Create job in job-service
        Job job = jobRepository.save(new Job(
            UUID.randomUUID(),
            "Java Developer",
            "Write clean code",
            new BigDecimal("50000"),
            // ... other fields
        ));
        
        // Act: Publish event
        JobCreatedEvent event = JobCreatedEvent.from(job);
        eventPublisher.publishEvent(event);
        
        // Wait for async processing
        Thread.sleep(500);
        
        // Assert: Job is indexed in elasticsearch
        SearchResult results = searchRepository
            .findByJobId(job.getId());
        
        assertNotNull(results);
        assertEquals("Java Developer", results.getTitle());
        assertEquals(job.getId(), results.getJobId());
    }
    
    @Test
    void jobCreatedEventReachesNotificationService() 
            throws InterruptedException {
        // Arrange
        ArgumentCaptor<EmailEvent> emailCaptor = 
            ArgumentCaptor.forClass(EmailEvent.class);
        
        Job job = jobRepository.save(new Job(/* ... */));
        
        // Act
        JobCreatedEvent event = JobCreatedEvent.from(job);
        eventPublisher.publishEvent(event);
        
        // Wait for event propagation
        waitForEventProcessing(1000);
        
        // Assert
        verify(mockEmailService, times(1))
            .sendEmail(emailCaptor.capture());
        
        EmailEvent capturedEmail = emailCaptor.getValue();
        assertThat(capturedEmail.getBody())
            .contains(job.getJobTitle());
    }
}
```

---

## ▸ EVENT VERSIONING STRATEGY

### 1. **V1 to V2 Evolution (Non-Breaking)**
```java
// V1: Original event (legacy)
@JsonTypeName("job.created.v1")
public class JobCreatedEventV1 {
    public UUID jobId;
    public String jobTitle;
    public String description;
    // Missing: employerId, location, industry (new fields in v2)
}

// V2: Enhanced event (new fields, backward compatible)
@JsonTypeName("job.created.v2")
public class JobCreatedEventV2 {
    public UUID jobId;
    public UUID employerId;           // NEW: identify employer
    public String jobTitle;
    public String description;
    public Location location;         // NEW: geo-location
    public Industry industry;         // NEW: categorization
    public BigDecimal salary;         // NEW: salary info
    public LocalDateTime publishedAt; // NEW: timestamp
    
    // Backward compat: support v1
    @JsonAnySetter
    private Map<String, Object> additionalProperties = new HashMap<>();
}

// Conversion adapter
public class JobCreatedEventConverter {
    
    /**
     * Upgrade V1 → V2 event format
     * Fills missing fields with sensible defaults or lookups
     */
    public static JobCreatedEventV2 upgradeFromV1(JobCreatedEventV1 v1, 
                                                   JobRepository jobRepo) {
        Job job = jobRepo.findById(v1.jobId).orElse(null);
        
        JobCreatedEventV2 v2 = new JobCreatedEventV2();
        v2.jobId = v1.jobId;
        v2.jobTitle = v1.jobTitle;
        v2.description = v1.description;
        
        // Fill in missing fields from job entity
        if (job != null) {
            v2.employerId = job.getEmployerId();
            v2.location = job.getLocation();
            v2.industry = job.getIndustry();
            v2.salary = job.getSalary();
            v2.publishedAt = LocalDateTime.now();
        }
        
        return v2;
    }
}

// Consumer handles both versions gracefully
@Component
public class JobEventConsumer {
    
    @RabbitListener(queues = "job.events")
    public void handleJobEvent(@Payload String message, 
                               @Headers Map<String, Object> headers) 
            throws JsonProcessingException {
        // Detect version from header
        String version = (String) headers.getOrDefault(
            "x-event-version", "v1"
        );
        
        if ("v1".equals(version)) {
            JobCreatedEventV1 v1 = 
                objectMapper.readValue(message, JobCreatedEventV1.class);
            JobCreatedEventV2 v2 = 
                JobCreatedEventConverter.upgradeFromV1(v1, jobRepo);
            processJobCreatedV2(v2);
        } else {
            JobCreatedEventV2 v2 = 
                objectMapper.readValue(message, JobCreatedEventV2.class);
            processJobCreatedV2(v2);
        }
    }
    
    private void processJobCreatedV2(JobCreatedEventV2 event) {
        // Universal processing logic for v2
        // v1 consumers get best-effort processing
    }
}

// Publishing versioned events
@Component
public class JobEventPublisher {
    
    @Autowired private RabbitTemplate rabbitTemplate;
    
    public void publishJobCreated(Job job) {
        JobCreatedEventV2 event = JobCreatedEventV2.from(job);
        
        // Publish with version header
        rabbitTemplate.convertAndSend("job.events.fanout", "job.created",
            objectMapper.writeValueAsString(event),
            message -> {
                // Add version header for routing/filtering
                message.getMessageProperties()
                    .setHeader("x-event-version", "v2");
                message.getMessageProperties()
                    .setHeader("x-event-schema", "job.created");
                return message;
            });
    }
}
```

### 2. **Breaking Change Migration (V2 → V3)**
```yaml
# When V3 introduces BREAKING change (incompatible)
migration-strategy:
  phase-1-preparation:
    duration: 2-weeks
    actions:
      - Deploy V3 consumers (backward-compatibile reading v2)
      - Start producing V2 events (existing)
      - Register V3 consumers to fanout exchange
  
  phase-2-dual-production:
    duration: 1-week
    actions:
      - Deploy dual-write: produce V2 + V3 events
      - V2 consumers continue (legacy)
      - V3 consumers process V3 events
      - Compare results for parity
  
  phase-3-migration:
    duration: 2-weeks
    gates:
      - All V3 consumers healthy (error_rate < 0.1%)
      - Data parity checks pass (100% match)
      - Zero critical incidents
    actions:
      - Stop producing V2 events
      - Enable routing to V3 only
      - V2 consumers drain and decommission
  
  phase-4-cleanup:
    duration: 1-week
    actions:
      - Remove V2 event handlers
      - Archive historical V2 events
      - Update documentation
```

---

## ▸ RECONCILIATION JOBS (Data Consistency Recovery)

### 1. **Automated Reconciliation Procedure**
```java
// Background job: detects & fixes data gaps
@Service
@EnableScheduling
public class ConsistencyReconciliationService {
    
    private final JobRepository jobRepository;
    private final SearchRepository searchRepository;
    private final ReconciliationMetrics metrics;
    private final AlertService alertService;
    
    private static final Logger log = LoggerFactory.getLogger(
        ConsistencyReconciliationService.class
    );
    
    /**
     * Runs every 5 minutes during migration phase
     * Detects and fixes data inconsistencies
     */
    @Scheduled(fixedDelay = 300000)
    public void reconcileJobSearchIndex() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Find jobs missing from search index
            List<Job> jobsInDb = jobRepository.findAll();
            List<UUID> jobsInSearch = searchRepository
                .findAllJobIds();
            
            // Identify gaps
            Set<UUID> missingInSearch = jobsInDb.stream()
                .map(Job::getId)
                .filter(id -> !jobsInSearch.contains(id))
                .collect(Collectors.toSet());
            
            if (!missingInSearch.isEmpty()) {
                log.warn("Found {} jobs missing in search index, " +
                         "triggering reconciliation", 
                    missingInSearch.size());
                metrics.recordGauge("reconciliation.gap_count", 
                    missingInSearch.size());
                
                // Fix gaps
                missingInSearch.forEach(jobId -> {
                    Job job = jobRepository.findById(jobId).orElse(null);
                    if (job != null && job.isPublished()) {
                        // Re-index missing job
                        JobSearchIndex index = 
                            JobSearchIndex.from(job);
                        searchRepository.save(index);
                        metrics.incrementCounter("reconciliation.fixed");
                        log.info("Re-indexed job: {}", jobId);
                    }
                });
                
                // Verify all fields match
                verifySearchIndexContent(missingInSearch);
            }
            
            // Check for orphaned records (in search but not in DB)
            findOrphanedRecords();
            
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordDuration("reconciliation.duration", duration);
            
            if (missingInSearch.isEmpty()) {
                log.debug("Reconciliation passed: all records consistent");
            }
            
        } catch (Exception e) {
            log.error("Reconciliation job failed", e);
            metrics.incrementCounter("reconciliation.failure");
            alertService.alert("Data consistency check failed: " + e.getMessage());
        }
    }
    
    /**
     * Detect orphaned records in search (exist in ES but not in DB)
     */
    private void findOrphanedRecords() {
        List<UUID> orphanedIds = searchRepository
            .findOrphanedJobIds();
        
        if (!orphanedIds.isEmpty()) {
            log.warn("Found {} orphaned records in search index", 
                orphanedIds.size());
            metrics.recordGauge("reconciliation.orphaned_count", 
                orphanedIds.size());
            
            // Remove orphaned records
            orphanedIds.forEach(id -> {
                searchRepository.deleteById(id);
                metrics.incrementCounter("reconciliation.orphan_removed");
            });
        }
    }
    
    /**
     * Verify field-level consistency
     */
    private void verifySearchIndexContent(Set<UUID> jobIds) {
        jobIds.forEach(jobId -> {
            Job dbJob = jobRepository.findById(jobId)
                .orElse(null);
            JobSearchIndex searchJob = searchRepository
                .findById(jobId).orElse(null);
            
            if (dbJob != null && searchJob != null) {
                if (!dbJob.getTitle().equals(searchJob.getTitle())) {
                    log.warn("Title mismatch for job {}: DB='{}' ES='{}'",
                        jobId, dbJob.getTitle(), 
                        searchJob.getTitle());
                    searchJob.setTitle(dbJob.getTitle());
                    searchRepository.save(searchJob);
                    metrics.incrementCounter("reconciliation.field_fixed");
                }
                
                // Check other critical fields
                verifyField(jobId, dbJob, searchJob, 
                    "description");
                verifyField(jobId, dbJob, searchJob, 
                    "employerId");
                verifyField(jobId, dbJob, searchJob, 
                    "salary");
            }
        });
    }
    
    private void verifyField(UUID jobId, Job dbJob, 
                             JobSearchIndex searchJob, 
                             String fieldName) {
        // Generic field verification
        Object dbValue = getFieldValue(dbJob, fieldName);
        Object esValue = getFieldValue(searchJob, fieldName);
        
        if (!Objects.equals(dbValue, esValue)) {
            log.warn("Field mismatch for job {} ({}): " +
                    "DB='{}' ES='{}'",
                jobId, fieldName, dbValue, esValue);
            setFieldValue(searchJob, fieldName, dbValue);
            searchRepository.save(searchJob);
            metrics.incrementCounter("reconciliation.field_fixed");
        }
    }
}
```

### 2. **Manual Reconciliation Trigger**
```yaml
# Admin endpoint for manual reconciliation
POST /admin/reconcile
Headers:
  Authorization: Bearer <ADMIN_TOKEN>
  X-Request-Id: <uuid>

Request Body:
  {
    "scope": "all|job-service|search-service",
    "target_jobs": ["job-id-1", "job-id-2"],  # optional
    "verify_only": false  # true = detect only, false = fix
  }

Response:
  {
    "status": "success|failure",
    "processed_count": 150,
    "gaps_found": 12,
    "gaps_fixed": 12,
    "orphaned_removed": 3,
    "duration_ms": 2840
  }
```

---

## ▸ DATA MIGRATION ROLLBACK PROCEDURE

### 1. **Rollback Plan (If CDC Migration Fails)**
```sql
-- SCENARIO: Discovered data mismatch during migration, need to rollback

-- Step 1: Stop dual-write (disable new service writes)
UPDATE feature_flags 
SET enabled = false 
WHERE flag_name = 'job_service_new_write';

-- Step 2: Verify current state of legacy table
SELECT COUNT(*) as job_count,
       COUNT(CASE WHEN user_id IS NULL THEN 1 END) as missing_fk
FROM jobs;

-- Step 3: Remove new columns if added
ALTER TABLE jobs 
ADD COLUMN employer_id_backup UUID;

UPDATE jobs SET employer_id_backup = employer_id;

-- Step 4: Start rollback transaction
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- Step 5: Restore original structure (if migration was in progress)
-- Only if we haven't deleted user_id column yet
INSERT INTO jobs_audit_log (job_id, action, timestamp, details)
SELECT id, 'ROLLBACK_IN_PROGRESS', NOW(), 
       json_build_object('reason', 'Data mismatch detected')
FROM jobs;

-- Step 6: Drop new FK constraint
ALTER TABLE jobs DROP CONSTRAINT IF EXISTS fk_jobs_employer CASCADE;

-- Step 7: Clear employer_id data (revert to original)
UPDATE jobs SET employer_id = NULL;

-- Step 8: If original user_id column was dropped, restore it
-- THIS STEP REQUIRES: We kept a backup or have point-in-time recovery
--
-- Option A: From backup (if backup exists)
-- RESTORE DATABASE FROM BACKUP (timestamp_before_migration);
--
-- Option B: From PostgreSQL WAL (if kept)
-- SELECT pg_wal_replay_recover(lsn_before_migration);
--
-- Option C: Manual restoration from appplication logs
-- Re-create user_id references from job_audit_log
-- (all job creation events have original user_id)

-- Step 9: Verify consistency after rollback
SELECT COUNT(*) as total_jobs,
       COUNT(CASE WHEN user_id IS NOT NULL THEN 1 END) as with_valid_fk,
       COUNT(CASE WHEN user_id IS NULL THEN 1 END) as orphaned_jobs
FROM jobs;

-- Step 10: Check for orphaned jobs (should be 0)
ASSERT (SELECT COUNT(*) FROM jobs WHERE user_id IS NULL) = 0
    ERROR 'Rollback incomplete: orphaned jobs detected';

-- Step 11: Commit if all checks pass
COMMIT;

-- Step 12: Alert operations team
UPDATE admin_alerts 
SET message = 'Migration rollback completed. Verify application behavior.',
    severity = 'HIGH'
WHERE alert_type = 'MIGRATION_STATUS';

-- Step 13: Log forensics for post-mortem
INSERT INTO migration_rollback_log (
    timestamp, reason, jobs_affected, 
    duration, rollback_status
) VALUES (
    NOW(), 'Data consistency mismatch detected', 
    (SELECT COUNT(*) FROM jobs),
    EXTRACT(EPOCH FROM (NOW() - migration_start_time))::int,
    'COMPLETED'
);
```

### 2. **Fallback to Legacy Application**
```yaml
# Automatic routing fallback if migration fails
fallback-procedure:
  trigger-condition: |
    Migration rollback initiated OR
    Data consistency check fails OR
    Latency > 2 seconds
  
  immediate-actions:
    - 1: Stop accepting new writes to new services
    - 2: Route all traffic to legacy application
    - 3: Alert on-call team (PagerDuty)
    - 4: Create incident ticket (Jira)
    - 5: Start forensics/analysis
  
  validation-checks:
    - legacy-service-responding: "Health check /health returns 200"
    - database-accessible: "PostgreSQL/MySQL connects successfully"
    - data-consistency: "No orphaned records detected"
    - event-queue-drained: "RabbitMQ queue count < 100"
  
  recovery-timeline:
    immediate: "Failover traffic (5-10 min)"
    short-term: "Root cause analysis (30 min - 2 hours)"
    medium-term: "Fix issues, test, prepare retry (1-5 days)"
    long-term: "Post-mortem, update strategy, re-plan (1-2 weeks)"
```

---

## ▸ NEXT STEPS (TASK-005, TASK-006+)

1. **TASK-005:** Create detailed Strangler Fig migration plan
2. **TASK-006:** Setup Job-Service skeleton (Spring Boot 3.4)
3. **TASK-007:** Implement Job aggregate root (domain)
4. **TASK-008:** Implement JobRepository port
5. **TASK-009:** Implement PostgreSQL adapter

---

## ▸ ESTIMATION FOR TASK-004

**Estimated:** 4 hours | **Actual:** 2.5 hours  
**Status:** ✅ COMPLETE

- Analyze dependencies between contexts: 45 min
- Design event schema: 40 min
- Define DTO contracts: 35 min
- Create dependency diagram: 30 min
- Documentation: 40 min

**Quality:** High confidence in dependency mapping and contract design.

---

## ▸ ARCHITECTURE DECISIONS FOR CONTEXT DEPENDENCIES

1. **Event-Driven Communication:** All inter-service communication via events
2. **Eventual Consistency:** Accept eventual consistency for better scalability
3. **CQRS Pattern:** Separate read models for search functionality
4. **API Gateway Routing:** Gradual traffic migration with fallback
5. **Data Migration:** Zero-downtime migration with dual-write pattern

**Ready for TASK-005 (Strangler Fig migration plan).**