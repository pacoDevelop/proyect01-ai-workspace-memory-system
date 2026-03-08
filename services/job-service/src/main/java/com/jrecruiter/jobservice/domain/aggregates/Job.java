package com.jrecruiter.jobservice.domain.aggregates;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.jrecruiter.jobservice.domain.events.JobClosedEvent;
import com.jrecruiter.jobservice.domain.events.JobDomainEvent;
import com.jrecruiter.jobservice.domain.events.JobHeldEvent;
import com.jrecruiter.jobservice.domain.events.JobPublishedEvent;
import com.jrecruiter.jobservice.domain.events.JobResumedEvent;
import com.jrecruiter.jobservice.domain.exceptions.InvalidJobException;
import com.jrecruiter.jobservice.domain.exceptions.InvalidJobStateException;
import com.jrecruiter.jobservice.domain.valueobjects.CompanyName;
import com.jrecruiter.jobservice.domain.valueobjects.JobDescription;
import com.jrecruiter.jobservice.domain.valueobjects.JobLocation;
import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;
import com.jrecruiter.jobservice.domain.valueobjects.JobSalary;
import com.jrecruiter.jobservice.domain.valueobjects.JobTitle;
import com.jrecruiter.jobservice.domain.valueobjects.OfferedBy;

/**
 * Job Aggregate Root
 * 
 * Core domain entity representing a job posting in the Job-Service bounded context.
 * Enforces all business invariants and rules.
 * 
 * Lifecycle:
 * 1. DRAFT: Created but not published (from createDraft factory)
 * 2. PUBLISHED: Visible to candidates (from publish method)
 * 3. ON_HOLD: Temporarily hidden (from hold method)
 * 4. CLOSED: Stopped accepting applications (from close method)
 * 5. ARCHIVED: Stored for audit (from archive method)
 * 
 * All fields are immutable after creation except status and timestamps.
 * 
 * @author GitHub Copilot / TASK-007
 */
public class Job {
    
    // IDENTITY
    private final UUID jobId;          // Surrogate key (database PK)
    private final String universalId;  // Business identifier (external systems)
    private final UUID employerId;     // Reference to employer (no FK in DB)
    
    // CORE DATA (Immutable after creation)
    private final JobTitle title;
    private final JobDescription description;
    private final CompanyName companyName;
    private final JobLocation location;
    private final JobSalary salary;
    private final OfferedBy offeredBy;
    private final UUID industryId;     // Reference to industry
    private final UUID regionId;       // Reference to region
    
    // MUTABLE STATE
    private JobPostingStatus status;
    private Instant createdAt;
    private Instant publishedAt;
    private Instant closedAt;
    private Instant updatedAt;
    
    // DOMAIN EVENTS (not persisted)
    private final List<JobDomainEvent> domainEvents;
    
    // ========================================================================
    // FACTORY METHODS
    // ========================================================================
    
    /**
     * Factory method: Create a new job in DRAFT status.
     *
     * Genera internamente:
     * - `jobId` (UUID) como PK.
     * - `universalId` (String) derivado del employer + secuencia.
     *
     * @param sequenceNumber Número secuencial por empleador (para universalId).
     * @throws InvalidJobException si algún invariante se viola.
     */
    public static Job createDraft(
            int sequenceNumber,
            UUID employerId,
            UUID industryId,
            UUID regionId,
            JobTitle title,
            JobDescription description,
            CompanyName companyName,
            JobLocation location,
            JobSalary salary,
            OfferedBy offeredBy) {

        Objects.requireNonNull(employerId, "employerId cannot be null");
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(companyName, "companyName cannot be null");
        Objects.requireNonNull(location, "location cannot be null");
        Objects.requireNonNull(salary, "salary cannot be null");
        Objects.requireNonNull(offeredBy, "offeredBy cannot be null");

        if (sequenceNumber <= 0) {
            throw new InvalidJobException("sequenceNumber must be positive");
        }

        UUID jobId = UUID.randomUUID();
        String universalId = "JOB-" + employerId.toString().substring(0, 8) + "-" + sequenceNumber;

        Job job = new Job(
                jobId,
                universalId,
                employerId,
                industryId,
                regionId,
                title,
                description,
                companyName,
                location,
                salary,
                offeredBy
        );

        job.status = JobPostingStatus.DRAFT;
        job.createdAt = Instant.now();
        job.updatedAt = job.createdAt;

        return job;
    }
    
    /**
     * Factory method: Reconstruct a job from database (event sourcing / persistence).
     * 
     * Used when loading persisted job from repository.
     */
    public static Job reconstruct(
            UUID jobId,
            String universalId,
            UUID employerId,
            UUID industryId,
            UUID regionId,
            JobTitle title,
            JobDescription description,
            CompanyName companyName,
            JobLocation location,
            JobSalary salary,
            OfferedBy offeredBy,
            JobPostingStatus status,
            Instant createdAt,
            Instant publishedAt,
            Instant closedAt,
            Instant updatedAt) {

        Job job = new Job(
                jobId,
                universalId,
                employerId,
                industryId,
                regionId,
                title,
                description,
                companyName,
                location,
                salary,
                offeredBy
        );

        job.status = status;
        job.createdAt = createdAt;
        job.publishedAt = publishedAt;
        job.closedAt = closedAt;
        job.updatedAt = updatedAt != null ? updatedAt : createdAt;

        return job;
    }
    
    // ========================================================================
    // PRIVATE CONSTRUCTOR
    // ========================================================================
    
    private Job(
            UUID jobId,
            String universalId,
            UUID employerId,
            UUID industryId,
            UUID regionId,
            JobTitle title,
            JobDescription description,
            CompanyName companyName,
            JobLocation location,
            JobSalary salary,
            OfferedBy offeredBy) {

        this.jobId = Objects.requireNonNull(jobId, "jobId cannot be null");
        this.universalId = Objects.requireNonNull(universalId, "universalId cannot be null");
        this.employerId = Objects.requireNonNull(employerId, "employerId cannot be null");
        this.industryId = industryId;
        this.regionId = regionId;
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.companyName = Objects.requireNonNull(companyName, "companyName cannot be null");
        this.location = Objects.requireNonNull(location, "location cannot be null");
        this.salary = Objects.requireNonNull(salary, "salary cannot be null");
        this.offeredBy = Objects.requireNonNull(offeredBy, "offeredBy cannot be null");
        this.domainEvents = new ArrayList<>();
    }
    
    // ========================================================================
    // BUSINESS OPERATIONS (State Transitions)
    // ========================================================================
    
    /**
     * Publish the job (transition: DRAFT → PUBLISHED).
     * 
     * Emits: JobPublishedEvent
     * 
     * @throws InvalidJobStateException if job cannot be published from current status
     */
    public void publish() {
        if (this.status != JobPostingStatus.DRAFT) {
            throw new InvalidJobStateException(
                "Cannot publish job in state: " + this.status
            );
        }
        
        this.status = JobPostingStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = Instant.now();
        
        // Emit event for downstream consumers
        this.domainEvents.add(new JobPublishedEvent(
            this.jobId,
            this.employerId,
            this.title,
            this.description,
            this.location,
            this.salary
        ));
    }
    
    /**
     * Close the job (transition: PUBLISHED/ON_HOLD → CLOSED).
     * 
     * Emits: JobClosedEvent
     * 
     * @param reason optional reason why job was closed
     * @throws InvalidJobStateException if job cannot be closed from current status
     */
    public void close(String reason) {
        if (this.status == JobPostingStatus.CLOSED || this.status == JobPostingStatus.ARCHIVED) {
            throw new InvalidJobStateException(
                "Cannot close job in state: " + this.status
            );
        }
        
        if (this.status == JobPostingStatus.DRAFT) {
            throw new InvalidJobStateException(
                "Cannot close job in DRAFT status. Publish first or discard."
            );
        }
        
        this.status = JobPostingStatus.CLOSED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();
        
        this.domainEvents.add(new JobClosedEvent(this.jobId, reason));
    }
    
    /**
     * Put job on hold (transition: PUBLISHED → ON_HOLD).
     * 
     * Emits: JobHeldEvent
     * 
     * @param reason optional reason why job was held
     * @throws InvalidJobStateException if job cannot be held from current status
     */
    public void hold(String reason) {
        if (this.status != JobPostingStatus.PUBLISHED) {
            throw new InvalidJobStateException(
                "Cannot hold job in state: " + this.status + ". Only published jobs can be held."
            );
        }
        
        this.status = JobPostingStatus.ON_HOLD;
        this.updatedAt = Instant.now();
        
        this.domainEvents.add(new JobHeldEvent(this.jobId, reason));
    }
    
    /**
     * Resume job (transition: ON_HOLD → PUBLISHED).
     * 
     * Emits: JobResumedEvent
     * 
     * @throws InvalidJobStateException if job cannot be resumed from current status
     */
    public void resume() {
        if (this.status != JobPostingStatus.ON_HOLD) {
            throw new InvalidJobStateException(
                "Cannot resume job in state: " + this.status + ". Only held jobs can be resumed."
            );
        }
        
        this.status = JobPostingStatus.PUBLISHED;
        this.updatedAt = Instant.now();
        
        this.domainEvents.add(new JobResumedEvent(this.jobId));
    }
    
    /**
     * Archive job (transition: CLOSED → ARCHIVED).
     * 
     * Final state. No further transitions possible.
     * 
     * @throws InvalidJobStateException if job cannot be archived from current status
     */
    public void archive() {
        if (this.status != JobPostingStatus.CLOSED) {
            throw new InvalidJobStateException(
                "Only closed jobs can be archived. Current status: " + this.status
            );
        }
        
        this.status = JobPostingStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }
    
    // ========================================================================
    // QUERY METHODS
    // ========================================================================
    
    /**
     * Check if job can transition to a specific status.
     */
    public boolean canTransitionTo(JobPostingStatus targetStatus) {
        return this.status.canTransitionTo(targetStatus);
    }
    
    /**
     * Check if job is currently published and visible.
     */
    public boolean isPublished() {
        return this.status == JobPostingStatus.PUBLISHED;
    }
    
    /**
     * Check if job is closed (not accepting applications).
     */
    public boolean isClosed() {
        return this.status == JobPostingStatus.CLOSED;
    }
    
    /**
     * Get all uncommitted domain events.
     */
    public List<JobDomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }
    
    /**
     * Clear domain events after they've been persisted.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ========================================================================
    // GETTERS
    // ========================================================================
    
    public UUID getJobId() {
        return jobId;
    }
    
    public String getUniversalId() {
        return universalId;
    }
    
    public UUID getEmployerId() {
        return employerId;
    }
    
    public JobTitle getTitle() {
        return title;
    }
    
    public JobDescription getDescription() {
        return description;
    }
    
    public CompanyName getCompanyName() {
        return companyName;
    }
    
    public JobLocation getLocation() {
        return location;
    }
    
    public JobSalary getSalary() {
        return salary;
    }
    
    public OfferedBy getOfferedBy() {
        return offeredBy;
    }
    
    public UUID getIndustryId() {
        return industryId;
    }
    
    public UUID getRegionId() {
        return regionId;
    }
    
    public JobPostingStatus getStatus() {
        return status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getPublishedAt() {
        return publishedAt;
    }
    
    public Instant getClosedAt() {
        return closedAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    // ========================================================================
    // EQUALITY & HASHCODE
    // ========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job job)) return false;
        return Objects.equals(jobId, job.jobId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
    
    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", universalId=" + universalId +
                ", title=" + title +
                ", status=" + status +
                ", employerId=" + employerId +
                '}';
    }
}
