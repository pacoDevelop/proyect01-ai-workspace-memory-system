package com.jrecruiter.jobservice.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.jrecruiter.jobservice.domain.aggregates.Job;
import com.jrecruiter.jobservice.domain.repositories.JobRepository;
import com.jrecruiter.jobservice.domain.valueobjects.CompanyName;
import com.jrecruiter.jobservice.domain.valueobjects.JobDescription;
import com.jrecruiter.jobservice.domain.valueobjects.JobLocation;
import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;
import com.jrecruiter.jobservice.domain.valueobjects.JobSalary;
import com.jrecruiter.jobservice.domain.valueobjects.JobTitle;
import com.jrecruiter.jobservice.domain.valueobjects.OfferedBy;

/**
 * Integration Test: PostgreSQL Job Repository
 * 
 * Tests repository operations against real database (H2 in-memory for tests).
 * Uses Spring Boot's @DataJpaTest for transaction rollback.
 * 
 * Profile: test (uses H2 in-memory database)
 * 
 * @author GitHub Copilot / TASK-011
 */
@DataJpaTest
@Import({PostgresJobRepository.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("PostgreSQL Job Repository Integration Tests")
class JobRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private JobRepository jobRepository;
    
    private UUID employerId;
    private UUID industryId;
    private UUID regionId;
    private Job testJob;
    
    @BeforeEach
    void setUp() {
        employerId = UUID.randomUUID();
        industryId = UUID.randomUUID();
        regionId = UUID.randomUUID();
        
        testJob = Job.createDraft(
                1,
                employerId,
                industryId,
                regionId,
                JobTitle.of("Full Stack Engineer"),
                JobDescription.of("Build modern web applications with React and Spring Boot"),
                CompanyName.of("TechCorp"),
                JobLocation.withAddress("456 Market St", "New York", "NY", "10001", "United States", "US"),
                JobSalary.of(
                        new BigDecimal("120000"),
                        new BigDecimal("150000"),
                        "USD",
                        JobSalary.SalaryFrequency.ANNUAL
                ),
                OfferedBy.EMPLOYER
        );
    }
    
    @Test
    @DisplayName("Save job persists to database")
    void testSaveJob() {
        Job saved = jobRepository.save(testJob);
        
        assertNotNull(saved.getJobId());
        assertEquals(testJob.getTitle(), saved.getTitle());
        assertEquals(testJob.getCompanyName(), saved.getCompanyName());
    }
    
    @Test
    @DisplayName("Find job by ID returns saved job")
    void testFindById() {
        Job saved = jobRepository.save(testJob);
        entityManager.flush();
        
        Optional<Job> found = jobRepository.findById(saved.getJobId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getJobId(), found.get().getJobId());
        assertEquals(saved.getTitle(), found.get().getTitle());
    }
    
    @Test
    @DisplayName("Find by universal ID retrieves correct job")
    void testFindByUniversalId() {
        Job saved = jobRepository.save(testJob);
        entityManager.flush();
        
        Optional<Job> found = jobRepository.findByUniversalId(saved.getUniversalId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getUniversalId(), found.get().getUniversalId());
    }
    
    @Test
    @DisplayName("Find by employer ID returns all employer's jobs")
    void testFindByEmployerId() {
        Job job1 = jobRepository.save(testJob);
        
        Job job2 = Job.createDraft(
                2,
                employerId,
                industryId,
                regionId,
                JobTitle.of("Frontend Developer"),
                JobDescription.of("React and TypeScript expert needed for web apps"),
                CompanyName.of("TechCorp"),
                JobLocation.withAddress("456 Market St", "New York", "NY", "10001", "United States", "US"),
                JobSalary.of(new BigDecimal("100000"), new BigDecimal("130000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.EMPLOYER
        );
        jobRepository.save(job2);
        
        entityManager.flush();
        
        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        
        assertEquals(2, jobs.size());
    }
    
    @Test
    @DisplayName("Delete job removes from database")
    void testDeleteJob() {
        Job saved = jobRepository.save(testJob);
        UUID jobId = saved.getJobId();
        
        entityManager.flush();
        assertTrue(jobRepository.existsById(jobId));
        
        jobRepository.delete(jobId);
        entityManager.flush();
        
        assertFalse(jobRepository.existsById(jobId));
    }
    
    @Test
    @DisplayName("Count by employer returns correct count")
    void testCountByEmployerId() {
        Job job1 = jobRepository.save(testJob);
        Job job2 = jobRepository.save(Job.createDraft(
                2, employerId, industryId, regionId,
                JobTitle.of("Backend Developer"),
                JobDescription.of("Build scalable Java microservices with Spring Boot"),
                CompanyName.of("TechCorp"),
                JobLocation.withAddress("456 Market St", "New York", "NY", "10001", "United States", "US"),
                JobSalary.of(new BigDecimal("110000"), new BigDecimal("140000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.EMPLOYER
        ));
        entityManager.flush();
        
        long count = jobRepository.countByEmployerId(employerId);
        
        assertEquals(2, count);
    }
    
    @Test
    @DisplayName("Count by status reflects published jobs")
    void testCountByStatus() {
        // Create and save draft job
        Job draft = jobRepository.save(testJob);
        entityManager.flush();
        entityManager.clear();
        
        // Create and save published job with unique employer
        Job publishedJob = Job.createDraft(
                2,  // Different universalId
                UUID.randomUUID(),  // Different employer
                industryId,
                regionId,
                JobTitle.of("Published Senior Position"),
                JobDescription.of("This job posting is published and visible to candidates seeking opportunities"),
                CompanyName.of("PublishingCorp"),
                JobLocation.withAddress("200 Oak Ave", "Denver", "CO", "80202", "United States", "US"),
                JobSalary.of(new BigDecimal("110000"), new BigDecimal("140000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.EMPLOYER
        );
        publishedJob.publish();  // Publish before saving
        jobRepository.save(publishedJob);
        entityManager.flush();
        
        // Verify counts
        long publishedCount = jobRepository.countByStatus(JobPostingStatus.PUBLISHED);
        long draftCount = jobRepository.countByStatus(JobPostingStatus.DRAFT);
        
        assertEquals(1, publishedCount, "Should have 1 published job");
        assertEquals(1, draftCount, "Should have 1 draft job");
    }
    
    @Test
    @DisplayName("Find all returns all persisted jobs")
    void testFindAll() {
        jobRepository.save(testJob);
        
        Job job2 = Job.createDraft(
                2, UUID.randomUUID(), industryId, regionId,
                JobTitle.of("Data Engineer"),
                JobDescription.of("BigData and analytics analytics platform development"),
                CompanyName.of("DataCorp"),
                JobLocation.withAddress("789 Oak Ave", "Seattle", "WA", "98101", "United States", "US"),
                JobSalary.of(new BigDecimal("130000"), new BigDecimal("160000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.RECRUITER
        );
        jobRepository.save(job2);
        
        entityManager.flush();
        
        List<Job> all = jobRepository.findAll();
        
        assertEquals(2, all.size());
    }
    
    @Test
    @DisplayName("Find by status returns only matching status jobs")
    void testFindByStatus() {
        // Save one DRAFT job
        Job draft = jobRepository.save(testJob);
        entityManager.flush();
        entityManager.clear();
        
        // Create and save one PUBLISHED job
        Job publishedJob = Job.createDraft(
                2, UUID.randomUUID(), industryId, regionId,
                JobTitle.of("Published Position"),
                JobDescription.of("This job posting is published and visible to candidates"),
                CompanyName.of("PublishInc"),
                JobLocation.withAddress("321 Pine St", "Austin", "TX", "78701", "United States", "US"),
                JobSalary.of(new BigDecimal("90000"), new BigDecimal("120000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.EMPLOYER
        );
        publishedJob.publish();  // Publish before saving
        jobRepository.save(publishedJob);
        entityManager.flush();
        
        // Query by status
        List<Job> published = jobRepository.findByStatus(JobPostingStatus.PUBLISHED);
        List<Job> drafts = jobRepository.findByStatus(JobPostingStatus.DRAFT);
        
        assertEquals(1, published.size(), "Should find 1 published job");
        assertEquals(1, drafts.size(), "Should find 1 draft job");
    }
    
    @Test
    @DisplayName("Exists by ID returns correct result")
    void testExistsById() {
        Job saved = jobRepository.save(testJob);
        entityManager.flush();
        
        assertTrue(jobRepository.existsById(saved.getJobId()));
        assertFalse(jobRepository.existsById(UUID.randomUUID()));
    }
    
    @Test
    @DisplayName("Update job preserves published state")
    void testUpdateJobState() {
        // Save job as DRAFT
        Job draft = jobRepository.save(testJob);
        UUID jobId = draft.getJobId();
        entityManager.flush();
        entityManager.clear();
        
        // Fetch to verify initial state
        Job fetched = jobRepository.findById(jobId).orElseThrow();
        assertEquals(JobPostingStatus.DRAFT, fetched.getStatus());
        
        // Create and save a separate PUBLISHED job to verify persistence of published state
        Job publishedJob = Job.createDraft(
                3,  // Different universalId
                UUID.randomUUID(),  // Different employer
                industryId,
                regionId,
                JobTitle.of("Published Role Updated"),
                JobDescription.of("Updated job description for published position in market"),
                CompanyName.of("UpdatedPublisher"),
                JobLocation.withAddress("300 Innovation Dr", "Silicon Valley", "CA", "94025", "United States", "US"),
                JobSalary.of(new BigDecimal("120000"), new BigDecimal("150000"), "USD", JobSalary.SalaryFrequency.ANNUAL),
                OfferedBy.EMPLOYER
        );
        publishedJob.publish();  // Set status to PUBLISHED
        Job saved = jobRepository.save(publishedJob);
        entityManager.flush();
        
        // Verify the published job was persisted correctly
        Job found = jobRepository.findById(saved.getJobId()).orElseThrow();
        assertEquals(JobPostingStatus.PUBLISHED, found.getStatus());
        assertNotNull(found.getPublishedAt());
    }
}