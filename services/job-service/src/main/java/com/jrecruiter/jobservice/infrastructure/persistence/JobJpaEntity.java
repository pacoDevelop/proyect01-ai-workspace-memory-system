package com.jrecruiter.jobservice.infrastructure.persistence;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;

/**
 * JPA Entity: Job
 * 
 * Persistence representation of the Job aggregate root.
 * Maps domain Model (Job aggregate) to relational database schema.
 * 
 * This is an ADAPTER layer entity - NOT part of the domain model.
 * Domain layer uses domain.aggregates.Job
 * 
 * Flyway migrations create the underlying 'jobs' table.
 * 
 * @author GitHub Copilot / TASK-009
 */
@Entity
@Table(name = "jobs")
public class JobJpaEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========================================================================
    // SURROGATE KEY (Auto-generated UUID)
    // ========================================================================
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID jobId;
    
    // ========================================================================
    // BUSINESS KEY (Universal Identifier)
    // ========================================================================
    
    @Column(name = "universal_id", length = 255, unique = true, nullable = false)
    private String universalId;
    
    // ========================================================================
    // REFERENCES (Foreign Keys)
    // ========================================================================
    
    @Column(name = "employer_id", nullable = false, columnDefinition = "UUID")
    private UUID employerId;
    
    @Column(name = "industry_id", nullable = true, columnDefinition = "UUID")
    private UUID industryId;
    
    @Column(name = "region_id", nullable = true, columnDefinition = "UUID")
    private UUID regionId;
    
    // ========================================================================
    // VALUE OBJECTS (Embedded Components)
    // ========================================================================
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "company_name", length = 255, nullable = false)
    private String companyName;
    
    @Embedded
    private JobLocationEmbeddable location;
    
    @Embedded
    private JobSalaryEmbeddable salary;
    
    @Column(name = "offered_by", length = 50, nullable = false)
    private String offeredBy;
    
    // ========================================================================
    // MUTABLE STATE
    // ========================================================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private JobPostingStatus status;
    
    // ========================================================================
    // TIMESTAMPS
    // ========================================================================
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;
    
    @Column(name = "published_at", nullable = true, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant publishedAt;
    
    @Column(name = "closed_at", nullable = true, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant closedAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;
    
    // ========================================================================
    // OPTIMISTIC LOCKING (Concurrency Control)
    // ========================================================================
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================
    
    public JobJpaEntity() {
        // JPA-required no-arg constructor
    }
    
    public JobJpaEntity(
            UUID jobId,
            String universalId,
            UUID employerId,
            UUID industryId,
            UUID regionId,
            String title,
            String description,
            String companyName,
            JobLocationEmbeddable location,
            JobSalaryEmbeddable salary,
            String offeredBy,
            JobPostingStatus status,
            Instant createdAt,
            Instant publishedAt,
            Instant closedAt,
            Instant updatedAt) {
        this.jobId = jobId;
        this.universalId = universalId;
        this.employerId = employerId;
        this.industryId = industryId;
        this.regionId = regionId;
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
        this.offeredBy = offeredBy;
        this.status = status;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.closedAt = closedAt;
        this.updatedAt = updatedAt;
    }
    
    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================
    
    public UUID getJobId() {
        return jobId;
    }
    
    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
    
    public String getUniversalId() {
        return universalId;
    }
    
    public void setUniversalId(String universalId) {
        this.universalId = universalId;
    }
    
    public UUID getEmployerId() {
        return employerId;
    }
    
    public void setEmployerId(UUID employerId) {
        this.employerId = employerId;
    }
    
    public UUID getIndustryId() {
        return industryId;
    }
    
    public void setIndustryId(UUID industryId) {
        this.industryId = industryId;
    }
    
    public UUID getRegionId() {
        return regionId;
    }
    
    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public JobLocationEmbeddable getLocation() {
        return location;
    }
    
    public void setLocation(JobLocationEmbeddable location) {
        this.location = location;
    }
    
    public JobSalaryEmbeddable getSalary() {
        return salary;
    }
    
    public void setSalary(JobSalaryEmbeddable salary) {
        this.salary = salary;
    }
    
    public String getOfferedBy() {
        return offeredBy;
    }
    
    public void setOfferedBy(String offeredBy) {
        this.offeredBy = offeredBy;
    }
    
    public JobPostingStatus getStatus() {
        return status;
    }
    
    public void setStatus(JobPostingStatus status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Instant getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}
