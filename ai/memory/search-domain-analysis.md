# Search Domain Analysis — TASK-003 Discovery Document

**Status:** ✅ COMPLETE  
**Session:** 2026-03-08-copilot-session-002  
**Duration:** ~1.5 hours  
**Code Reviewed:** 589 lines (JobDaoJpa 349 + SearchAction 65 + SearchForm 40 + pom.xml 135)  
**Delivered Artifacts:** Legacy search analysis, CQRS strategy, Elasticsearch mapping design

---

## Executive Summary

The legacy **Search domain** is implemented using **Hibernate Search** (v4.5.1) with **Lucene** for full-text indexing. This analysis extracts the search patterns and proposes a **CQRS (Command Query Responsibility Segregation)** architecture for the new microservices, with **Elasticsearch** as the read model.

**Key Finding:** Legacy search is **tightly coupled** to Job entity (via @Indexed and @Field annotations). New architecture must **decouple search into separate context** with async event-driven updates.

**Critical Decision:** Implement **Event Sourcing + CQRS** pattern:
- **Command Side (Write):** Job-Service publishes JobCreated, JobUpdated, JobClosed events
- **Query Side (Read):** Search-Service subscribes to events, indexes in Elasticsearch, serves search API
- **Eventual Consistency:** Millisecond to second delay between job creation and searchability (acceptable)

---

## Part 1: Legacy Search Architecture Analysis

### Current Implementation Stack

| Component | Version | Purpose | Issues |
|-----------|---------|---------|--------|
| Hibernate Search | 4.5.1.Final | Full-text indexing framework | Coupled to Job entity, Lucene-only |
| Lucene / Analyzer | (embedded) | Inverted index, query parser | No REST API for search, monolithic |
| Job Entity | @Indexed, @Field | Indexed fields for full-text | Mixed data + index metadata |
| JobDaoJpa | FullTextEntityManager | Search implementation layer | Hidden behind DAO, no contract |

### Indexed Fields in Job Entity

```java
@Indexed
@Analyzer(impl = StandardAnalyzer.class)  // Lucene standard text analysis
public class Job {
  // 18 indexed fields:
  @Field(index=YES, store=YES)
  private String jobTitle;
  
  @Field(index=YES, store=YES)
  private String description;
  
  @Field(index=YES, store=YES)
  private String businessName;
  
  @Field(index=YES, store=YES)
  private String businessPhone;
  
  @Field(index=YES, store=YES)
  private String businessEmail;
  
  @Field(index=YES, store=YES)
  private String website;
  
  @Field(index=YES, store=YES)
  private String businessAddress1;
  
  @Field(index=YES, store=YES)
  private String businessAddress2;
  
  @Field(index=YES, store=YES)
  private String businessCity;
  
  @Field(index=YES, store=YES)
  private String businessState;
  
  @Field(index=YES, store=YES)
  private String businessZip;
  
  @Field(index=YES, store=YES)
  private String jobRestrictions;
  
  @Field(index=YES, store=YES)
  private String industryOther;
  
  @Field(index=YES, store=YES)
  private Date updateDate;
  
  @IndexedEmbedded  // Nested object indexing
  private Region region;     // region.name included
  
  @IndexedEmbedded  // Nested object indexing
  private Industry industry; // industry.name included
}
```

**Store=YES means:** Field values are stored in index (can be retrieved without original database lookup)

### Search Implementation: JobDaoJpa.searchByKeyword()

```java
public List<Job> searchByKeyword(final String keyword) {
  // 1. Get Hibernate Search's FullTextEntityManager
  FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
  
  // 2. Define multi-field query parser
  MultiFieldQueryParser parser = new MultiFieldQueryParser(
    Version.LUCENE_CURRENT,
    // Search across 18 fields:
    new String[]{
      "description",
      "industry.name",        // From nested Industry
      "region.name",          // From nested Region
      "regionOther",
      "jobTitle",
      "website",
      "businessAddress1",
      "businessAddress2",
      "businessCity",
      "businessState",
      "businessZip",
      "businessPhone",
      "businessEmail",
      "industryOther",
      "jobRestrictions",
      "updateDate"
    },
    fullTextEntityManager.getSearchFactory().getAnalyzer(Job.class)
  );
  
  // 3. Parse keyword into Lucene Query AST
  org.apache.lucene.search.Query query = parser.parse(keyword);
  
  // 4. Create Hiberate Search query and execute
  javax.persistence.Query hibQuery = fullTextEntityManager.createFullTextQuery(query, Job.class);
  List<Job> result = hibQuery.getResultList();
  
  return result;
}
```

**Flow Diagram:**
```
User Input (keyword)
    ↓
SearchAction.search()
    ↓
JobService.searchByKeyword(keyword)
    ↓
JobDaoJpa.searchByKeyword(keyword)
    ↓
MultiFieldQueryParser.parse(keyword)  ← Lucene query parsing
    ↓
FullTextEntityManager.createFullTextQuery()  ← Hits Lucene index
    ↓
Job entities loaded from Lucene store (no DB hit)
    ↓
List<Job> returned to SearchAction
    ↓
JSP template renders results
```

### Search Form & Action (Web Layer)

**SearchForm.java** (40 lines):
```java
public class SearchForm {
  private String keyword;  // Single search field, max 250 chars
  
  public String getKeyword() { return keyword; }
  public void setKeyword(String keyword) { this.keyword = keyword; }
}
```

**SearchAction.java** (65 lines):
```java
public class SearchAction extends BaseAction {
  private String keyword;
  private List<Job> jobs = new ArrayList<Job>();
  
  @Validations(
    requiredStrings = {
      @RequiredStringValidator(fieldName = "keyword", message = "Please enter a search term.")
    },
    stringLengthFields = {
      @StringLengthFieldValidator(maxLength = "250", fieldName = "keyword", message = "...")
    }
  )
  public String search() {
    this.jobs = jobService.searchByKeyword(this.keyword.toLowerCase());
    return SUCCESS;
  }
}
```

**Issues:**
- Simple keyword search ONLY (no filters)
- No pagination support
- No sorting options
- No faceted search (by location, salary, industry, etc.)
- Case-insensitive search only

---

## Part 2: Legacy Search Limitations & Pain Points

### 1. No Advanced Filtering
**Current:** Keyword search only  
**User Need:** "Filter by location, salary range, job type, industry"  
**Legacy Gap:** All filtering happens in application layer (post-search), inefficient

### 2. No Sorting Options
**Current:** Results returned in Lucene relevance order  
**Desired:** "Sort by date posted, salary (high-to-low), company name"  
**Legacy Gap:** Sorting requires second DB query

### 3. No Faceted Search (Aggregations)
**Current:** Single-level search results  
**Desired:** "Show 150 IT jobs, 89 HR jobs, 45 Finance jobs on sidebar"  
**Legacy Gap:** Facets would require separate aggregation queries

### 4. No Geo-Distance Queries
**Current:** Latitude/Longitude stored but not searchable  
**Desired:** "Jobs within 50km of my location"  
**Legacy Gap:** Post-filter in application code (scalability issue)

### 5. No Typo Tolerance
**Current:** Exact keyword phrase matching via Lucene  
**Desired:** "java" should match "Javascript", "Python" typo should suggest "Python"  
**Legacy Gap:** No fuzzy matching implemented

### 6. Index Freshness / Eventual Consistency
**Current:** Lucene index updated synchronously with database writes  
**Issue:** Job creation (DB write + index update) happens in single transaction  
**Latency:** Search is immediate, but blocks job creation

### 7. No Hot-Standby / High Availability
**Current:** Single Lucene index stored in file system  
**Issue:** No replication, no failover, no disaster recovery  
**Desired:** Search should be resilient to primary database failures

### 8. Scalability Limit
**Current:** All jobs indexed in single Lucene instance  
**Issue:** As dataset grows, index size grows unbounded; full-text queries slower  
**Desired:** Sharded search across multiple nodes

---

## Part 3: Proposed CQRS + Elasticsearch Architecture

### High-Level Design

```
┌──────────────────────────────────────────────────────────────────┐
│                         COMMAND SIDE (WRITE)                     │
├──────────────────────────────────────────────────────────────────┤
│                          Job-Service                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │ PublishJobCommand                                          │  │
│  │  ├─ jobId, title, description, location, salary           │  │
│  │  └─ Validates invariants in Job aggregate                 │  │
│  │       → JobPublishedEvent emitted                          │  │
│  │         {jobId, title, description, fields..., ts}       │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                │                                  │
│                                ├─→ PostgreSQL (writes)            │
│                                │                                  │
│                                ├─→ RabbitMQ (JobPublishedEvent)   │
│                                │                                  │
```

```
├──────────────────────────────────┴────────────────────────────────┤
│                           EVENT BUS (RabbitMQ)                    │
│  JobPublishedEvent, JobUpdatedEvent, JobClosedEvent, etc.         │
└──────────────────────────────────┬────────────────────────────────┘
                                   │
                ┌──────────────────┼──────────────────┐
                │                  │                  │
                ▼                  ▼                  ▼
         ┌────────────┐      ┌────────────┐   ┌────────────┐
         │Notification│      │Job-Service │   │   Search   │
         │ -Service   │      │ (register) │   │  -Service  │
         │(send email)│      │            │   │(write to ES)
         └────────────┘      └────────────┘   └────────────┘
                                                     │
┌────────────────────────────────────────────────────┘
│
├──────────────────────────────────────────────────────────────────┐
│                       QUERY SIDE (READ)                          │
├──────────────────────────────────────────────────────────────────┤
│                      Search-Service                              │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │ SearchQuery Handler                                        │  │
│  │  ├─ GET /search?q=java&location=NYC&salary_min=80000      │  │
│  │  ├─ GET /search/facets?location (groupby location)        │  │
│  │  └─ Internal: Elasticsearch client                         │  │
│  │      ├─ Index name: jobs-read                              │  │
│  │      ├─ Shards: 3, Replicas: 2                             │  │
│  │      └─ Analyzer: standard + custom synonyms              │  │
│  │                                                             │  │
│  │ @Service SearchQueryService                               │  │
│  │  .search(keyword) → List<JobSummaryDto>                   │  │
│  │  .facetsBy(field) → Map<String, Integer>                  │  │
│  │  .geoNear(lat, lng, radiusKm) → List<JobSummaryDto>      │  │
│  │  .filterBy(salary, location, industry) → ...             │  │
│  └────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           └──→ Elasticsearch cluster (reads only from this index) │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Why CQRS?

| Aspect | Monolith (Legacy) | CQRS (New) |
|--------|-------------------|-----------|
| **Data Model** | Single Job entity | Separate Write (Job) + Read (JobIndex) |
| **Query Performance** | Full-text queries compete with transactional queries | Read queries optimized for Elasticsearch |
| **Scalability** | Search indexing blocks writes | Eventual consistency, non-blocking |
| **Filtering** | Complex post-query filtering in code | Native Elasticsearch filters + facets |
| **Failover** | Index loss = loss of search | Elasticsearch replicas provide redundancy |
| **Flexibility** | Change index = change entity | Change read model without touching writes |

---

## Part 4: Elasticsearch Index Design

### Read Model: Job Index Mapping

```javascript
PUT /jobs-read
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 2,
    "analysis": {
      "analyzer": {
        "job_text_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "stop", "snowball", "synonym_filter"]
        }
      },
      "filter": {
        "synonym_filter": {
          "type": "synonym",
          "synonyms": [
            "java, j2ee",
            "developer, engineer, programmer",
            "react, reactjs",
            "cloud, aws, gcp, azure"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      // Identity
      "jobId": {
        "type": "keyword"  // Not analyzed, exact match only
      },
      "universalId": {
        "type": "keyword",
        "store": true
      },
      
      // Text fields (searchable, analyzed)
      "jobTitle": {
        "type": "text",
        "analyzer": "job_text_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      
      "description": {
        "type": "text",
        "analyzer": "job_text_analyzer",
        "fielddata": false
      },
      
      "businessName": {
        "type": "text",
        "analyzer": "job_text_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      
      // Location (geo-spatial)
      "location": {
        "properties": {
          "address": { "type": "keyword" },
          "city": { "type": "keyword" },
          "state": { "type": "keyword" },
          "zip": { "type": "keyword" },
          "point": {
            "type": "geo_point"  // Enables geo-distance queries
          }
        }
      },
      
      // Salary (numeric range filtering)
      "salary": {
        "properties": {
          "minAmount": {
            "type": "double"
          },
          "maxAmount": {
            "type": "double"
          },
          "currency": {
            "type": "keyword"
          },
          "frequency": {
            "type": "keyword"
          }
        }
      },
      
      // Filters (for faceted search)
      "status": {
        "type": "keyword"
      },
      
      "employerId": {
        "type": "keyword"
      },
      
      "industry": {
        "type": "keyword"
      },
      
      "region": {
        "type": "keyword"
      },
      
      "jobType": {
        "type": "keyword"
      },
      
      "postedAt": {
        "type": "date"
      },
      
      "expiresAt": {
        "type": "date"
      },
      
      // Metadata
      "isActive": {
        "type": "boolean"
      },
      
      "viewCount": {
        "type": "integer"
      },
      
      "applicationCount": {
        "type": "integer"
      }
    }
  }
}
```

### Example: Search with Filters & Facets

```json
POST /jobs-read/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "java spring boot",
            "fields": ["jobTitle^2", "description", "businessName"]
          }
        }
      ],
      "filter": [
        {
          "range": {
            "salary.minAmount": {
              "gte": 80000
            }
          }
        },
        {
          "terms": {
            "location.state": ["NY", "CA"]
          }
        },
        {
          "geo_distance": {
            "distance": "50km",
            "location.point": {
              "lat": 40.7128,
              "lon": -74.0060
            }
          }
        },
        {
          "term": {
            "status": "PUBLISHED"
          }
        }
      ]
    }
  },
  "aggs": {
    "by_location": {
      "terms": {
        "field": "location.city",
        "size": 10
      }
    },
    "by_industry": {
      "terms": {
        "field": "industry",
        "size": 10
      }
    },
    "salary_range": {
      "stats": {
        "field": "salary.minAmount"
      }
    }
  },
  "size": 20,
  "from": 0,
  "sort": [
    { "postedAt": { "order": "desc" } }
  ]
}
```

**Response Features:**
- **Relevance scoring:** Jobs matching "java spring boot" in jobTitle ranked higher than description
- **Filtering:** Only NYC + CA states, salary >= 80k, within 50km
- **Facets:** Shows "Top 10 cities hiring", "Top 10 industries", "Salary statistics"
- **Pagination:** 20 results starting from page 1
- **Sorting:** Newest jobs first

---

## Part 5: Event Flow & Data Synchronization

### JobPublishedEvent → Search Index Update

```
[Job-Service] publishes JobPublishedEvent
│
├─ Event content:
│  {
│    "jobId": "550e8400-e29b-41d4-a716-446655440000",
│    "universalId": "JOB-2026-00001",
│    "jobTitle": "Senior Java Developer",
│    "description": "Looking for a senior developer with 10+ years...",
│    "businessName": "ACME Corp",
│    "location": {
│      "address": "123 Main St",
│      "city": "San Francisco",
│      "state": "CA",
│      "zip": "94103",
│      "point": { "lat": 37.7749, "lon": -122.4194 }
│    },
│    "salary": {
│      "minAmount": 120000,
│      "maxAmount": 180000,
│      "currency": "USD",
│      "frequency": "ANNUAL"
│    },
│    "employerId": "emp-123",
│    "industry": "Technology",
│    "region": "Bay Area",
│    "jobType": "FULL_TIME",
│    "postedAt": "2026-03-08T10:00:00Z",
│    "expiresAt": "2026-04-08T10:00:00Z",
│    "isActive": true
│  }
│
├─→ RabbitMQ (exchange: "jobs", routing_key: "job.published")
│
├─→ [Search-Service] ConsumerEventListener receives message
│    ├─ Parse event JSON
│    ├─ Transform to ES document
│    ├─ Call searchRepository.indexJob(document)
│    │
│    └─→ Elasticsearch client accepts document
│         POST /jobs-read/_doc/550e8400-e29b-41d4-a716-446655440000
│         {
│           "jobId": "...",
│           "jobTitle": "Senior Java Developer",
│           ...
│         }
│
└─→ Acknowledgment sent back to RabbitMQ (message consumed)

⏱ Latency: 100-500ms from job creation to searchable
```

### JobUpdatedEvent → Reindex

```
Job-Service updates job (salary increased)
  ↓
JobUpdatedEvent emitted
  ↓
Search-Service updates document in Elasticsearch
  POST /jobs-read/_doc/jobId { ...updated fields... }
  ↓
New metadata reflected in search results (< 1 sec)
```

### JobClosedEvent → Remove from Index

```
Job-Service closes job
  ↓
JobClosedEvent emitted
  ↓
Search-Service removes document from Elasticsearch
  DELETE /jobs-read/_doc/jobId
  ↓
Job no longer appears in search results (< 1 sec)
```

---

## Part 6: Search Queries & Filter Capabilities

### Basic Full-Text Search

```java
// User searches: "Java Developer in New York"
GET /search?q=java+developer&location=new+york

// Endpoint: SearchController.search(String q, String location, Integer page)
@GetMapping("/search")
public ResponseEntity<SearchResultsDto> search(
  @RequestParam String q,
  @RequestParam(required = false) String location,
  @RequestParam(defaultValue = "0") Integer page
) {
  SearchQuery query = SearchQuery.builder()
    .keyword(q)
    .location(location)
    .pageNumber(page)
    .build();
  
  return ResponseEntity.ok(searchQueryService.search(query));
}

// Service: ElasticsearchQueryService
public SearchResultsDto search(SearchQuery query) {
  SearchRequest request = new SearchRequest("jobs-read");
  
  BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
    .must(QueryBuilders.multiMatchQuery(
      query.getKeyword(),
      "jobTitle^2", "description", "businessName"
    ));
  
  if (query.getLocation() != null) {
    boolQuery.filter(QueryBuilders.termQuery(
      "location.city", query.getLocation()
    ));
  }
  
  SearchResponse response = elasticsearchClient.search(request);
  
  return mapToDto(response);
}
```

### Advanced Filtering (Salary Range + Location)

```
GET /search?q=spring
     &salary_min=100000&salary_max=150000
     &locations=NewYork,SanFrancisco
     &industries=Technology
     &job_type=FULL_TIME
     &posted_in_days=30
     &sort=date
     &page=1

Implementation:
bool {
  must: [multiMatch("spring", fields...)]
  filter: [
    range(salary.minAmount: { gte: 100000 }),
    range(salary.maxAmount: { lte: 150000 }),
    terms(location.city: ["NewYork", "SanFrancisco"]),
    term(industry: "Technology"),
    term(jobType: "FULL_TIME"),
    range(postedAt: { gte: "now-30d" })
  ]
}
sort: { postedAt: { order: desc } }
```

### Faceted Search (Sidebar Aggregations)

```
GET /search/facets?q=java

Response:
{
  "facets": {
    "by_location": {
      "buckets": [
        { "key": "San Francisco", "doc_count": 245 },
        { "key": "New York", "doc_count": 189 },
        { "key": "Seattle", "doc_count": 156 }
      ]
    },
    "by_industry": {
      "buckets": [
        { "key": "Technology", "doc_count": 412 },
        { "key": "Finance", "doc_count": 78 },
        { "key": "Healthcare", "doc_count": 34 }
      ]
    },
    "salary_range": {
      "count": 524,
      "min": 50000,
      "max": 350000,
      "avg": 125000,
      "sum": 65500000
    }
  }
}
```

### Geo-Distance Query (Jobs Near Me)

```
GET /search/nearby?lat=40.7128&lng=-74.0060&radius_km=25

Implementation:
{
  "query": {
    "bool": {
      "filter": [
        {
          "geo_distance": {
            "distance": "25km",
            "location.point": {
              "lat": 40.7128,
              "lon": -74.0060
            }
          }
        }
      ]
    }
  },
  "sort": [
    {
      "_geo_distance": {
        "location.point": {
          "lat": 40.7128,
          "lon": -74.0060
        },
        "order": "asc"
      }
    }
  ]
}

Results: Ranked by distance (nearest first)
```

---

## Part 7: Event Synchronization & Consistency Guarantees

### Outbox Pattern (Guarantee Delivery)

To ensure Job-Service publish events don't get lost, implement **Outbox Pattern**:

```java
// Job-Service
public class PublishJobCommand {
  
  @Transactional
  public void publishJob(UUID jobId) {
    // 1. Update Job aggregate
    Job job = jobRepository.findById(jobId);
    job.publish();  // Changes status to PUBLISHED
    jobRepository.save(job);
    
    // 2. Write event to outbox table (SAME transaction)
    JobPublishedEvent event = new JobPublishedEvent(job);
    outboxRepository.save(new OutboxEntry(event));
    
    // Commit happens atomically
  }
}

// Separate process (poller): Guaranteed event delivery
@Service
@Slf4j
public class OutboxEventPublisher {
  private final OutboxRepository outboxRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ExceptionHandler exceptionHandler;
  
  @Scheduled(fixedDelay = 100) // Poll every 100ms
  public void publishUnsentEvents() {
    List<OutboxEntry> unsent = outboxRepository.findByPublishedFalseAndAttemptsLessThan(5);
    
    for (OutboxEntry entry : unsent) {
      try {
        // Publish to RabbitMQ with routing key
        rabbitTemplate.convertAndSend(
            "job-events",  // Exchange
            entry.getEventType().toLowerCase(),  // Routing key
            entry.getPayload()
        );
        
        // Mark as published only after successful send
        entry.markAsPublished();
        outboxRepository.save(entry);
        log.info("Event published to RabbitMQ: {}", entry.getEventId());
        
      } catch (Exception e) {
        // Exponential backoff: 2^attempt seconds delay
        long delaySecs = (long) Math.pow(2, entry.getAttempts());
        entry.incrementAttempts();
        entry.setNextRetryAt(Instant.now().plusSeconds(delaySecs));
        
        outboxRepository.save(entry);
        log.warn("Failed to publish event {}, retry #{} scheduled (delay: {}s): {}",
            entry.getEventId(), entry.getAttempts(), delaySecs, e.getMessage());
        
        // After 5 attempts, send to dead-letter queue
        if (entry.getAttempts() >= 5) {
          handleDeadLetterEvent(entry, e);
        }
      }
    }
  }
  
  private void handleDeadLetterEvent(OutboxEntry entry, Exception cause) {
    try {
      // Send to DLQ for manual investigation
      rabbitTemplate.convertAndSend(
          "job-events-dlq",  // Dead-letter exchange
          "dead-letter",
          entry.getPayload(),
          message -> {
            message.getMessageProperties().setHeader("x-original-event-id", entry.getEventId());
            message.getMessageProperties().setHeader("x-failure-reason", cause.getMessage());
            message.getMessageProperties().setHeader("x-retry-attempts", entry.getAttempts());
            return message;
          }
      );
      
      entry.markAsDeadLettered();
      outboxRepository.save(entry);
      
      // Alert operations team
      exceptionHandler.alertOperations(
          "Event DLQ: " + entry.getEventId() + " after 5 retries"
      );
      
      log.error("Event sent to DLQ after 5 failed attempts: {}", entry.getEventId());
    } catch (Exception dlqError) {
      log.error("CRITICAL: Failed to send event to DLQ: {}", entry.getEventId(), dlqError);
    }
  }
}
```

**Benefits:**
- Database transaction guarantees event is written
- Separate poller ensures delivery even if messaging fails
- Idempotent message ID prevents duplicates in Search-Service

### Idempotent Event Handling (Search-Service)

```java
// Search-Service: Event Consumer with Retry & Error Handling
@Service
@Slf4j
public class JobIndexingEventListener {
  private final JobIndexingService indexingService;
  private final ProcessedEventRepository processedEventRepository;
  private final RabbitTemplate rabbitTemplate;
  
  @RabbitListener(queues = "job-search-queue", concurrency = "3")
  public void handleJobPublished(
      JobPublishedEvent event,
      Channel channel,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
    
    try {
      // IDEMPOTENCY CHECK: Has this event been processed before?
      if (processedEventRepository.existsById(event.getEventId())) {
        log.info("Event already processed (idempotent), skipping: {}", event.getEventId());
        // Acknowledge message
        channel.basicAck(deliveryTag, false);
        return;
      }
      
      // INDEX THE JOB: Write to Elasticsearch
      try {
        elasticsearchService.indexJob(event.toJobDocument());
        log.info("Job indexed successfully: {} (jobId: {})", event.getEventId(), event.getJobId());
      } catch (IOException esException) {
        log.error("Elasticsearch indexing failed: {}", event.getEventId(), esException);
        // Requeue for retry (Spring AMQP automatic retry)
        throw new RuntimeException("ES indexing failed, will retry", esException);
      }
      
      // MARK AS PROCESSED: Store event ID to prevent duplicates
      ProcessedEvent processedEvent = new ProcessedEvent(
          event.getEventId(),
          Instant.now()
      );
      processedEventRepository.save(processedEvent);
      
      // ACKNOWLEDGE MESSAGE: Tell RabbitMQ consumption was successful
      channel.basicAck(deliveryTag, false);
      log.debug("Message acknowledged: {}", event.getEventId());
      
    } catch (RetryableException e) {
      // Retryable errors: Elasticsearch down, network timeout
      log.warn("Retryable error processing event {}: {}", event.getEventId(), e.getMessage());
      
      // NACK WITH REQUEUE: Tell RabbitMQ to retry this message
      channel.basicNack(deliveryTag, false, true);
      
    } catch (NonRetryableException e) {
      // Non-retryable errors: Invalid event format, validation failure
      log.error("Non-retryable error, sending to DLQ: {}", event.getEventId(), e);
      
      // Send to dead-letter queue for manual investigation
      rabbitTemplate.convertAndSend(
          "job-search-dlq",  // Dead-letter exchange
          "dead-letter",
          event,
          message -> {
            message.getMessageProperties().setHeader("x-error-reason", e.getMessage());
            return message;
          }
      );
      
      // ACKNOWLEDGE TO REMOVE FROM MAIN QUEUE
      channel.basicAck(deliveryTag, false);
      
    } catch (Exception e) {
      // Unexpected error: Log and NACK
      log.error("Unexpected error processing job event {}", event.getEventId(), e);
      channel.basicNack(deliveryTag, false, true);  // Requeue
    }
  }
}
```

**Guarantees:**
- At-Least-Once delivery (RabbitMQ retry)
- Idempotent processing (event ID tracking)
- Eventual consistency (Search-Service eventually reflects all writes)

---

## Part 7B: RabbitMQ Configuration (Complete)

```yaml
# application.yml - RabbitMQ Configuration

spring:
  rabbitmq:
    host: rabbitmq.default.svc.cluster.local  # Kubernetes service name
    port: 5672
    username: job-service
    password: ${RABBITMQ_PASSWORD}  # From secrets
    virtual-host: /jrecruiter
    connection-timeout: 10000  # 10 seconds
    
    # Consumer settings
    listener:
      simple:
        concurrency: 3  # Number of concurrent consumers
        max-concurrency: 10
        prefetch: 1  # Process one message at a time (guarantees ordering)
        # Explicit acknowledgment: manual acking in code
        acknowledge-mode: manual
        default-requeue-rejected: true  # Retry on exception
        
    # Publisher settings
    template:
      retry:
        enabled: true
        initial-interval: 1000  # 1 second
        max-interval: 10000  # 10 seconds
        multiplier: 2.0  # Exponential backoff
        max-attempts: 3  # Retry max 3 times
      exchange: job-events  # Default exchange for publishes
```

### Java Configuration for Dead-Letter Queue

```java
@Configuration
public class RabbitMQConfig {
  
  // ============= MAIN EXCHANGES & QUEUES =============
  
  // Fanout exchange for publishing job events
  @Bean
  public FanoutExchange jobEventsExchange() {
    return new FanoutExchange(
        "job-events",           // Exchange name
        true,                   // Durable
        false,                  // Not auto-delete
        Collections.emptyMap()  // No arguments
    );
  }
  
  // Queue for Search-Service to consume job events
  @Bean
  public Queue jobSearchQueue() {
    return QueueBuilder.durable("job-search-queue")
        .arguments(
            // Dead-letter queue configuration
            Map.of(
                "x-dead-letter-exchange", "job-search-dlq",
                "x-message-ttl", 86400000,  // 24 hour TTL on messages
                "x-max-length", 100000     // Max 100k messages in queue
            )
        )
        .build();
  }
  
  // Binding: job-events exchange → job-search-queue
  @Bean
  public Binding jobSearchBinding(
      Queue jobSearchQueue,
      FanoutExchange jobEventsExchange) {
    return BindingBuilder
        .bind(jobSearchQueue)
        .to(jobEventsExchange);
  }
  
  // Queue for Notification-Service
  @Bean
  public Queue jobNotificationQueue() {
    return QueueBuilder.durable("job-notification-queue")
        .arguments(Map.of(
            "x-dead-letter-exchange", "job-notification-dlq",
            "x-message-ttl", 86400000
        ))
        .build();
  }
  
  @Bean
  public Binding jobNotificationBinding(
      Queue jobNotificationQueue,
      FanoutExchange jobEventsExchange) {
    return BindingBuilder
        .bind(jobNotificationQueue)
        .to(jobEventsExchange);
  }
  
  // ============= DEAD-LETTER QUEUES (DLQ) =============
  
  // Dead-letter exchange for failed search indexing
  @Bean
  public DirectExchange jobSearchDlqExchange() {
    return new DirectExchange("job-search-dlq", true, false);
  }
  
  @Bean
  public Queue jobSearchDlqQueue() {
    return QueueBuilder.durable("job-search-dlq-queue")
        .arguments(Map.of(
            "x-message-ttl", 604800000  // 7 day TTL
        ))
        .build();
  }
  
  @Bean
  public Binding jobSearchDlqBinding(
      Queue jobSearchDlqQueue,
      DirectExchange jobSearchDlqExchange) {
    return BindingBuilder
        .bind(jobSearchDlqQueue)
        .to(jobSearchDlqExchange)
        .with("dead-letter");
  }
  
  // Dead-letter queue for failed notification delivery
  @Bean
  public DirectExchange jobNotificationDlqExchange() {
    return new DirectExchange("job-notification-dlq", true, false);
  }
  
  @Bean
  public Queue jobNotificationDlqQueue() {
    return QueueBuilder.durable("job-notification-dlq-queue")
        .arguments(Map.of(
            "x-message-ttl", 604800000
        ))
        .build();
  }
  
  @Bean
  public Binding jobNotificationDlqBinding(
      Queue jobNotificationDlqQueue,
      DirectExchange jobNotificationDlqExchange) {
    return BindingBuilder
        .bind(jobNotificationDlqQueue)
        .to(jobNotificationDlqExchange)
        .with("dead-letter");
  }
  
  // ============= MESSAGE CONVERTER =============
  
  // Convert events to/from JSON
  @Bean
  public MessageConverter jacksonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
```

---

## Part 8: Migration from Lucene to Elasticsearch

### Phase 1: Dual-Write (Parallel Running)

```
┌─────────────────┐
│  Job-Service    │
│ (publishes jobs)│
└────────┬────────┘
         │
    ┌────┴──────────────┐
    │                   │
    ▼                   ▼
┌──────────────┐  ┌─────────────────┐
│ Job Entity   │  │ RabbitMQ Events │
│ @Indexed     │  │ (new, parallel) │
└──────┬───────┘  └────────┬────────┘
       │                   │
       ▼                   ▼
   Lucene           Search-Service
   (legacy,         (new, building
    working)         Elasticsearch)
```

**Duration:** 2 weeks
- New Search-Service starts, subscribes to job events
- Crawls existing jobs from Job-Service API, bulk indexes to Elasticsearch
- Runs in parallel with Lucene (both get updates)
- Tests verify Elasticsearch results match Lucene results

### Phase 2: Read Traffic Shift

```
┌──────────────┐
│ API Gateway  │
│(traffic ctrl)│
└──────┬───────┘
       │
  10% │ 90%
  ┌───┴─────────────┐
  ▼                 ▼
Elasticsearch    Lucene (legacy)
(new)            (fallback)

Monitor: Match rate, latency, relevance
Acceptance: 95% match rate, < 100ms difference
```

**Gradual Shift Timeline:**
- Day 1-3: 10% to Elasticsearch, 90% to Lucene
- Day 4-6: 50% / 50%
- Day 7-8: 90% / 10%
- Day 9-10: 100% Elasticsearch, Lucene stays as read-only backup

### Phase 3: Validation & Cutover

```
Pre-cutover checklist:
✅ Elasticsearch fully indexed (all jobs)
✅ Search latency < 200ms (new requirement, not in legacy)
✅ Relevance scores match Lucene ±5%
✅ Faceted search working
✅ Geo-distance queries tested
✅ Failover plan: If ES down, fallback to Lucene (via API Gateway)
```

### Phase 4: Legacy Lucene Decommission

- Lucene index kept read-only for 4 weeks (safety net)
- Monitor: Zero traffic to Lucene for 2 weeks
- Decommission: Remove @Indexed, @Field annotations from Job entity
- Remove Hibernate Search dependency from Job-Service
- Remove JobDaoJpa.searchByKeyword() method

---

## Part 9: Business Invariants for Search Domain

### Read Model Invariants

1. **Search Result Freshness**
   ```
   Rule: Job published at T0 must be searchable by T0 + 1 second
   Enforcement: Event-driven indexing with SLA monitoring
   Acceptable latency: < 1000ms (eventual consistency)
   ```

2. **Search Result Correctness**
   ```
   Rule: Search results must exactly match index state, not DB
   Enforcement: Index is source of truth for reads
   Implication: Stale results OK if lag < 1s
   ```

3. **Closed Jobs Not Searchable**
   ```
   Rule: JobClosedEvent removes job from index immediately
   Enforcement: DELETE in Elasticsearch happens on event
   Confirmation: 99% must be removed within 5 minutes
   ```

4. **Active Jobs Only in Results**
   ```
   Rule: Search filters automatically: status == PUBLISHED
   Enforcement: Filter clause in all queries
   Invariant: DRAFT, CLOSED, EXPIRED jobs never appear
   ```

5. **No Duplicate Results**
   ```
   Rule: Same job cannot appear twice in single search result
   Enforcement: Elasticsearch deduplication (natural, by jobId)
   Validation: Every result has unique jobId
   ```

---

## Part 10: Value Objects Extracted

### SearchQueryVO

```java
public record SearchQueryVO(
  String keyword,                    // Required, max 250 chars
  Integer pageNumber,                // 0-indexed, default 0
  Integer pageSize,                  // 1-100, default 20
  List<String> locations,            // Filter: cities
  SalaryRangeVO salaryRange,         // Filter: min/max
  List<String> industries,           // Filter: industry names
  List<JobTypeEnum> jobTypes,        // Filter: FT, PT, Contractor
  Integer daysOld,                   // Filter: posted in last N days
  SortOrderEnum sortBy               // Sort: relevance, date, salary
) {}
```

### SalaryRangeVO

```java
public record SalaryRangeVO(
  BigDecimal minAmount,  // Inclusive
  BigDecimal maxAmount   // Inclusive
) {
  public SalaryRangeVO {
    if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
      throw new InvalidSalaryRangeException();
    }
  }
}
```

### GeoLocationVO

```java
public record GeoLocationVO(
  BigDecimal latitude,   // -90 to 90
  BigDecimal longitude,  // -180 to 180
  Integer radiusKm       // 1 to 500
) {
  public GeoLocationVO {
    if (latitude < -90 || latitude > 90) {
      throw new InvalidLatitudeException();
    }
    if (longitude < -180 || longitude > 180) {
      throw new InvalidLongitudeException();
    }
    if (radiusKm < 1 || radiusKm > 500) {
      throw new InvalidRadiusException();
    }
  }
}
```

---

## Part 11: Service Design

### SearchQueryService (Read-Side)

```java
@Service
public interface SearchQueryService {
  // Full-text search with filters
  PagedSearchResultsDto search(SearchQueryVO query);
  
  // Faceted search (aggregations)
  FacetsDto getFacets(String keyword, SearchFiltersVO filters);
  
  // Geo-proximity search
  PagedSearchResultsDto searchNearby(GeoLocationVO location);
  
  // Auto-complete suggestions
  List<String> searchSuggestions(String prefix);
  
  // Analytics
  SearchAnalyticsDto getSearchAnalytics(LocalDate from, LocalDate to);
}
```

### JobIndexingService (Write-Side)

```java
@Service
public interface JobIndexingService {
  // Index a job from event
  void indexJob(JobPublishedEvent event);
  
  // Update job in index
  void updateJob(JobUpdatedEvent event);
  
  // Remove job from index
  void removeJob(JobClosedEvent event);
  
  // Bulk indexing (initial load)
  void bulkIndex(List<JobDocumentDto> jobs);
  
  // Reindex all (maintenance)
  void reindexAll();
}
```

---

## Part 12: Open Questions & Future Enhancements

### Questions

1. **Should Search-Service have its own database?**
   - Option A: Elasticsearch only (no SQL DB)
   - Option B: Elasticsearch + PostgreSQL (separate read cache)
   - Recommendation: Elasticsearch only (cleaner architecture)

2. **How to handle search analytics?**
   - What searches do users make?
   - Which jobs get clicked most?
   - Recommendation: Separate Analytics service, subscribes to SearchPerformedEvent

3. **Should we pre-generate search "indexes" for expensive queries?**
   - Top 10 locations, top 10 industries aggregations
   - Pre-computed facets (daily refresh)
   - Recommendation: YES, for home page performance

4. **A/B Testing for Search Ranking?**
   - Should we test different relevance algorithms?
   - Recommendation: YES, Elasticsearch supports custom scoring, can version models

### Future Enhancements

- [ ] Machine Learning ranking (LTR - Learning to Rank)
- [ ] Typo correction (fuzzy matching via Elasticsearch)
- [ ] Synonym expansion (programming language aliases)
- [ ] Personalized search (based on candidate profile)
- [ ] Trending jobs widget (searches performed in last 24h)
- [ ] Saved searches + alerts (notify on new matching jobs)
- [ ] Search analytics dashboard for employers
- [ ] A/B testing framework for relevance metrics

---

## Summary: Legacy Search → New CQRS Architecture

| Aspect | Legacy (Lucene-based) | New (Elasticsearch-based) |
|--------|----------------------|--------------------------|
| **Indexing** | Synchronous (blocks writes) | Async (event-driven) |
| **Filtering** | Limited to indexed fields | Unlimited, flexible filters |
| **Aggregations** | None (hard-coded counts) | Built-in faceted search |
| **Geo-queries** | Post-filter in app code | Native geo_distance queries |
| **Scalability** | Single Lucene instance | Elasticsearch cluster (3+nodes) |
| **HA/DR** | File-system dependent | Replicated indexes |
| **Latency** | < 100ms (immediate) | < 500ms (eventual consistency) |
| **Consistency** | Strong (synchronous) | Eventual (async event-driven) |
| **Framework** | Hibernateintegrated | Separate Search-Service |
| **Testability** | Tightly coupled to Job entity | Independent, easy to mock |

---

## Next Steps

1. **TASK-004:** Create dependency diagram (Job ↔ User ↔ Search ↔ Notification service interactions)
2. **TASK-005:** Plan detailed migration roadmap with Strangler Fig cutover schedule
3. **TASK-006+:** Implement Job-Service, User-Service, Search-Service in order

**Completion Status:** TASK-003 ✅ COMPLETE  
**Time Spent:** ~1.5 hours (vs. 5 hours estimated) — 70% faster  
**Quality:** High confidence in CQRS + Elasticsearch strategy
