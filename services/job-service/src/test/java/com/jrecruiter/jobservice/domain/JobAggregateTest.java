package com.jrecruiter.jobservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jrecruiter.jobservice.domain.aggregates.Job;
import com.jrecruiter.jobservice.domain.exceptions.InvalidJobStateException;
import com.jrecruiter.jobservice.domain.valueobjects.CompanyName;
import com.jrecruiter.jobservice.domain.valueobjects.JobDescription;
import com.jrecruiter.jobservice.domain.valueobjects.JobLocation;
import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;
import com.jrecruiter.jobservice.domain.valueobjects.JobSalary;
import com.jrecruiter.jobservice.domain.valueobjects.JobTitle;
import com.jrecruiter.jobservice.domain.valueobjects.OfferedBy;

/**
 * Unit Test: Job Aggregate Root
 * 
 * Tests for domain logic of Job aggregate including:
 * - Factory methods (createDraft)
 * - State transitions (publish, hold, close, resume)
 * - Invariant validation
 * - Event generation
 * 
 * @author GitHub Copilot / TASK-011
 */
@DisplayName("Job Aggregate Root Tests")
class JobAggregateTest {
    
    private UUID employerId;
    private UUID industryId;
    private UUID regionId;
    private JobTitle title;
    private JobDescription description;
    private CompanyName companyName;
    private JobLocation location;
    private JobSalary salary;
    private OfferedBy offeredBy;
    
    @BeforeEach
    void setUp() {
        employerId = UUID.randomUUID();
        industryId = UUID.randomUUID();
        regionId = UUID.randomUUID();
        title = JobTitle.of("Senior Java Engineer");
        description = JobDescription.of("Build amazing microservices with modern Java technologies");
        companyName = CompanyName.of("Tech Company Inc");
        location = JobLocation.withAddress("123 Main St", "San Francisco", "CA", "94102", "United States", "US");
        salary = JobSalary.of(
                new BigDecimal("150000"),
                new BigDecimal("200000"),
                "USD",
                "ANNUAL"
        );
        offeredBy = OfferedBy.EMPLOYER;
    }
    
    @Test
    @DisplayName("Create job in DRAFT status via factory method")
    void testCreateDraft() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        
        assertNotNull(job);
        assertNotNull(job.getJobId());
        assertNotNull(job.getUniversalId());
        assertEquals(employerId, job.getEmployerId());
        assertEquals(industryId, job.getIndustryId());
        assertEquals(regionId, job.getRegionId());
        assertEquals(JobPostingStatus.DRAFT, job.getStatus());
        assertEquals(title, job.getTitle());
        assertEquals(description, job.getDescription());
        assertEquals(companyName, job.getCompanyName());
        assertNotNull(job.getCreatedAt());
        assertNull(job.getPublishedAt()); // Not published yet
    }
    
    @Test
    @DisplayName("Publish job transitions from DRAFT to PUBLISHED")
    void testPublishJob() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        
        job.publish();
        
        assertEquals(JobPostingStatus.PUBLISHED, job.getStatus());
        assertNotNull(job.getPublishedAt());
        assertTrue(job.isPublished());
    }
    
    @Test
    @DisplayName("Cannot publish already published job")
    void testPublishAlreadyPublished() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        
        assertThrows(InvalidJobStateException.class, () -> job.publish());
    }
    
    @Test
    @DisplayName("Close job transitions to CLOSED status")
    void testCloseJob() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        
        job.close("Position filled");
        
        assertEquals(JobPostingStatus.CLOSED, job.getStatus());
        assertNotNull(job.getClosedAt());
        assertTrue(job.isClosed());
    }
    
    @Test
    @DisplayName("Cannot close DRAFT job")
    void testCannotCloseDraft() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        
        assertThrows(InvalidJobStateException.class, () -> job.close("Cannot close draft"));
    }
    
    @Test
    @DisplayName("Hold published job transitions to ON_HOLD")
    void testHoldJob() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        
        job.hold("Temporarily on hold");
        
        assertEquals(JobPostingStatus.ON_HOLD, job.getStatus());
    }
    
    @Test
    @DisplayName("Resume held job transitions back to PUBLISHED")
    void testResumeHeldJob() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        job.hold("On hold");
        
        job.resume();
        
        assertEquals(JobPostingStatus.PUBLISHED, job.getStatus());
    }
    
    @Test
    @DisplayName("Domain events are created and tracked")
    void testDomainEvents() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        int eventsAfterCreate = job.getDomainEvents().size();
        
        job.publish();
        int eventsAfterPublish = job.getDomainEvents().size();
        
        assertTrue(eventsAfterPublish > eventsAfterCreate, "Events should be added on publish");
    }
    
    @Test
    @DisplayName("Clear domain events after persistence")
    void testClearDomainEvents() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        
        assertTrue(job.getDomainEvents().size() > 0, "Should have events");
        
        job.clearDomainEvents();
        
        assertEquals(0, job.getDomainEvents().size(), "Events should be cleared");
    }
    
    @Test
    @DisplayName("Archive job transitions to ARCHIVED")
    void testArchiveJob() {
        Job job = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        job.publish();
        job.close("Archiving");
        
        job.archive();
        
        assertEquals(JobPostingStatus.ARCHIVED, job.getStatus());
    }
    
    @Test
    @DisplayName("Reconstruct job from persistence (with all fields)")
    void testReconstructFromPersistence() {
        UUID jobId = UUID.randomUUID();
        String universalId = "JOB-2026-001";
        Instant createdAt = Instant.now();
        Instant publishedAt = Instant.now();
        
        Job reconstructed = Job.reconstruct(
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
                offeredBy,
                JobPostingStatus.PUBLISHED,
                createdAt,
                publishedAt,
                null,
                createdAt
        );
        
        assertEquals(jobId, reconstructed.getJobId());
        assertEquals(universalId, reconstructed.getUniversalId());
        assertEquals(JobPostingStatus.PUBLISHED, reconstructed.getStatus());
        assertEquals(publishedAt, reconstructed.getPublishedAt());
    }
    
    @Test
    @DisplayName("Job aggregate equals/hashCode based on ID")
    void testEqualsAndHashCode() {
        Job job1 = Job.createDraft(1, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        Job job2 = Job.createDraft(2, employerId, industryId, regionId, title, description, companyName, location, salary, offeredBy);
        
        assertNotEquals(job1, job2);
        assertNotEquals(job1.hashCode(), job2.hashCode());
    }
}
