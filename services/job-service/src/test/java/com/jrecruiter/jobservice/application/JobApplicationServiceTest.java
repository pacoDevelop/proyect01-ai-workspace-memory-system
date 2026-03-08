package com.jrecruiter.jobservice.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jrecruiter.jobservice.application.dtos.CreateJobRequest;
import com.jrecruiter.jobservice.application.dtos.JobResponse;
import com.jrecruiter.jobservice.application.dtos.PaginatedJobResponse;
import com.jrecruiter.jobservice.application.services.JobApplicationService;
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
 * Unit Test: Job Application Service
 * 
 * Tests application service logic with mocked repository.
 * Uses Mockito for dependency injection.
 * 
 * @author GitHub Copilot / TASK-011
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Job Application Service Unit Tests")
class JobApplicationServiceTest {
    
    @Mock
    private JobRepository jobRepository;
    
    @InjectMocks
    private JobApplicationService jobApplicationService;
    
    private UUID employerId;
    private UUID industryId;
    private UUID regionId;
    private Job testJob;
    private CreateJobRequest createRequest;
    
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
                JobTitle.of("Senior Architect"),
                JobDescription.of("Leading microservices architecture and team development"),
                CompanyName.of("ArchitectureCorp"),
                JobLocation.withAddress("999 Tech St", "San Jose", "CA", "95110", "United States", "US"),
                JobSalary.of(
                        new BigDecimal("180000"),
                        new BigDecimal("220000"),
                        "USD",
                        "ANNUAL"
                ),
                OfferedBy.EMPLOYER
        );
        
        createRequest = new CreateJobRequest(
                "Senior Architect",
                "Leading microservices architecture and team development",
                "ArchitectureCorp",
                new CreateJobRequest.LocationRequest("999 Tech St", "San Jose", "CA", "95110", "United States", "US", false),
                new CreateJobRequest.SalaryRequest(
                        new BigDecimal("180000"),
                        new BigDecimal("220000"),
                        "USD",
                        "ANNUAL"
                ),
                "EMPLOYER",
                industryId,
                regionId
        );
    }
    
    @Test
    @DisplayName("Create job calls repository and returns response")
    void testCreateJob() {
        when(jobRepository.countByEmployerId(employerId)).thenReturn(0L);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        JobResponse response = jobApplicationService.createJob(createRequest, employerId);
        
        assertNotNull(response);
        assertEquals("Senior Architect", response.getTitle());
        assertEquals("ArchitectureCorp", response.getCompanyName());
        verify(jobRepository).countByEmployerId(employerId);
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    @DisplayName("Get job by ID returns response when found")
    void testGetJobById() {
        UUID jobId = testJob.getJobId();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        
        JobResponse response = jobApplicationService.getJobById(jobId);
        
        assertNotNull(response);
        assertEquals(testJob.getTitle().getValue(), response.getTitle());
        verify(jobRepository).findById(jobId);
    }
    
    @Test
    @DisplayName("Get job by ID throws exception when not found")
    void testGetJobByIdNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, () -> jobApplicationService.getJobById(jobId));
    }
    
    @Test
    @DisplayName("Publish job transitions status and persists")
    void testPublishJob() {
        UUID jobId = testJob.getJobId();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        jobApplicationService.publishJob(jobId);
        
        assertEquals(JobPostingStatus.PUBLISHED, testJob.getStatus());
        verify(jobRepository).findById(jobId);
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    @DisplayName("Close job updates status and reason")
    void testCloseJob() {
        UUID jobId = testJob.getJobId();
        testJob.publish();
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        JobResponse response = jobApplicationService.closeJob(jobId, "Position filled");
        
        assertEquals("CLOSED", response.getStatus());
        assertNotNull(response.getClosedAt());
        verify(jobRepository).findById(jobId);
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    @DisplayName("Hold job transitions to ON_HOLD status")
    void testHoldJob() {
        UUID jobId = testJob.getJobId();
        testJob.publish();
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        JobResponse response = jobApplicationService.holdJob(jobId, "Temporary hold");
        
        assertEquals("ON_HOLD", response.getStatus());
        verify(jobRepository).findById(jobId);
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    @DisplayName("Resume held job returns to PUBLISHED")
    void testResumeJob() {
        UUID jobId = testJob.getJobId();
        testJob.publish();
        testJob.hold("Temp hold");
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        JobResponse response = jobApplicationService.resumeJob(jobId);
        
        assertEquals("PUBLISHED", response.getStatus());
        verify(jobRepository).findById(jobId);
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    @DisplayName("List published jobs returns paginated response")
    void testListPublishedJobs() {
        Job job2 = Job.createDraft(
                2, UUID.randomUUID(), industryId, regionId,
                JobTitle.of("DevOps Engineer"),
                JobDescription.of("Kubernetes and Docker expert"),
                CompanyName.of("CloudCorp"),
                JobLocation.withAddress("888 Cloud Ave", "Portland", "OR", "97201", "United States", "US"),
                JobSalary.of(new BigDecimal("140000"), new BigDecimal("170000"), "USD", "ANNUAL"),
                OfferedBy.EMPLOYER
        );
        
        when(jobRepository.findPublishedWithPagination(0, 20)).thenReturn(List.of(testJob, job2));
        when(jobRepository.countByStatus(JobPostingStatus.PUBLISHED)).thenReturn(2L);
        
        PaginatedJobResponse response = jobApplicationService.listPublishedJobs(0, 20);
        
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertFalse(response.isHasNext());
    }
    
    @Test
    @DisplayName("Delete draft job succeeds")
    void testDeleteDraftJob() {
        UUID jobId = testJob.getJobId();
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        
        jobApplicationService.deleteJob(jobId);
        
        verify(jobRepository).delete(jobId);
    }
    
    @Test
    @DisplayName("Delete published job throws exception")
    void testDeletePublishedJobThrows() {
        UUID jobId = testJob.getJobId();
        testJob.publish();
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        
        assertThrows(IllegalStateException.class, () -> jobApplicationService.deleteJob(jobId));
    }
    
    @Test
    @DisplayName("List jobs by employer returns employer's jobs with pagination")
    void testListJobsByEmployer() {
        UUID jobId = testJob.getJobId();
        when(jobRepository.findPublishedByEmployerIdWithPagination(employerId, 0, 20))
                .thenReturn(List.of(testJob));
        when(jobRepository.countByEmployerId(employerId)).thenReturn(1L);
        
        PaginatedJobResponse response = jobApplicationService.listJobsByEmployer(employerId, 0, 20);
        
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
    }
}
