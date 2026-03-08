package com.jrecruiter.jobservice.infrastructure.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jrecruiter.jobservice.application.dtos.CreateJobRequest;
import com.jrecruiter.jobservice.application.dtos.JobResponse;
import com.jrecruiter.jobservice.application.dtos.PaginatedJobResponse;
import com.jrecruiter.jobservice.application.dtos.UpdateJobRequest;
import com.jrecruiter.jobservice.application.services.JobApplicationService;

import jakarta.validation.Valid;

/**
 * REST Controller: Job Management API
 * 
 * Endpoints for job creation, retrieval, updates, and state transitions.
 * - X-Employer-ID header required for write operations
 * - Bean Validation annotations ensure request validity
 * - Returns 201 Created, 200 OK, 204 No Content, 404 Not Found, etc.
 * 
 * API Endpoints:
 * - POST   /api/jobs              Create new job
 * - GET    /api/jobs/:id          Get job by ID
 * - GET    /api/jobs/universal/:universalId  Get by universal ID
 * - PUT    /api/jobs/:id          Update job
 * - DELETE /api/jobs/:id          Delete job (DRAFT only)
 * - GET    /api/jobs              List published jobs (paginated)
 * - GET    /api/jobs/employer/:employerId  Get employer's jobs (paginated)
 * - POST   /api/jobs/:id/publish  Publish job
 * - POST   /api/jobs/:id/close    Close job
 * - POST   /api/jobs/:id/hold     Hold job
 * - POST   /api/jobs/:id/resume   Resume job
 * 
 * Authentication: Handled via security layer (not in this controller)
 * Authorization: X-Employer-ID header validates ownership
 * 
 * @author GitHub Copilot / TASK-010
 */
@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobController {
    
    private final JobApplicationService jobApplicationService;
    
    public JobController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }
    
    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================
    
    /**
     * Create a new job (creates in DRAFT status).
     * 
     * POST /api/jobs
     * Authorization: Required (via X-Employer-ID header)
     * 
     * @param request job creation request with validation
     * @param employerId the employer ID (extracted from auth context or header)
     * @return Created response with job details (201 Created)
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.createJob(request, employerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // ========================================================================
    // RETRIEVE OPERATIONS - By ID
    // ========================================================================
    
    /**
     * Get job by its UUID.
     * 
     * GET /api/jobs/{id}
     * Authorization: Optional (public read)
     * 
     * @param jobId the job UUID
     * @return job response (200 OK) or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable("id") UUID jobId) {
        JobResponse response = jobApplicationService.getJobById(jobId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get job by universal ID (business key).
     * 
     * GET /api/jobs/universal/{universalId}
     * Authorization: Optional (public read)
     * Useful for external system integration.
     * 
     * @param universalId the universal identifier
     * @return job response (200 OK) or 404 Not Found
     */
    @GetMapping("/universal/{universalId}")
    public ResponseEntity<JobResponse> getJobByUniversalId(@PathVariable("universalId") String universalId) {
        JobResponse response = jobApplicationService.getJobByUniversalId(universalId);
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // RETRIEVE OPERATIONS - Listing
    // ========================================================================
    
    /**
     * List published jobs (public listing with pagination).
     * 
     * GET /api/jobs?page=0&size=20
     * Authorization: Optional (public read)
     * 
     * @param pageNumber page number (0-based, default 0)
     * @param pageSize page size (default 20)
     * @return paginated job response (200 OK)
     */
    @GetMapping
    public ResponseEntity<PaginatedJobResponse> listPublishedJobs(
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "20") int pageSize) {
        PaginatedJobResponse response = jobApplicationService.listPublishedJobs(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List jobs by employer (employer-specific view with pagination).
     * 
     * GET /api/jobs/employer/{employerId}?page=0&size=20
     * Authorization: Required (employer can only see their own jobs)
     * 
     * @param employerId the employer UUID
     * @param pageNumber page number (0-based, default 0)
     * @param pageSize page size (default 20)
     * @return paginated job response (200 OK)
     */
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<PaginatedJobResponse> listJobsByEmployer(
            @PathVariable("employerId") UUID employerId,
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "20") int pageSize) {
        PaginatedJobResponse response = jobApplicationService.listJobsByEmployer(employerId, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================
    
    /**
     * Update job details (only for DRAFT jobs).
     * 
     * PUT /api/jobs/{id}
     * Authorization: Required (ownership validated)
     * 
     * @param jobId the job UUID
     * @param request update request
     * @param employerId the employer ID
     * @return updated job response (200 OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable("id") UUID jobId,
            @Valid @RequestBody UpdateJobRequest request,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.updateJob(jobId, request);
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // STATE TRANSITION OPERATIONS (POST to state endpoints)
    // ========================================================================
    
    /**
     * Publish a job (transition from DRAFT to PUBLISHED).
     * 
     * POST /api/jobs/{id}/publish
     * Authorization: Required
     * 
     * @param jobId the job UUID
     * @param employerId the employer ID
     * @return updated job response (200 OK)
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<JobResponse> publishJob(
            @PathVariable("id") UUID jobId,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.publishJob(jobId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Hold a job (transition to ON_HOLD status).
     * 
     * POST /api/jobs/{id}/hold
     * Authorization: Required
     * Body: JSON with optional "reason" field
     * 
     * @param jobId the job UUID
     * @param request request body with optional reason
     * @param employerId the employer ID
     * @return updated job response (200 OK)
     */
    @PostMapping("/{id}/hold")
    public ResponseEntity<JobResponse> holdJob(
            @PathVariable("id") UUID jobId,
            @RequestBody HoldJobRequest request,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.holdJob(jobId, request.getReason());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Resume a job (transition from ON_HOLD to PUBLISHED).
     * 
     * POST /api/jobs/{id}/resume
     * Authorization: Required
     * 
     * @param jobId the job UUID
     * @param employerId the employer ID
     * @return updated job response (200 OK)
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<JobResponse> resumeJob(
            @PathVariable("id") UUID jobId,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.resumeJob(jobId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Close a job (transition to CLOSED status).
     * 
     * POST /api/jobs/{id}/close
     * Authorization: Required
     * Body: JSON with optional "reason" field
     * 
     * @param jobId the job UUID
     * @param request request body with optional reason
     * @param employerId the employer ID
     * @return updated job response (200 OK)
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<JobResponse> closeJob(
            @PathVariable("id") UUID jobId,
            @RequestBody CloseJobRequest request,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        JobResponse response = jobApplicationService.closeJob(jobId, request.getReason());
        return ResponseEntity.ok(response);
    }
    
    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================
    
    /**
     * Delete a job (only DRAFT jobs can be deleted).
     * 
     * DELETE /api/jobs/{id}
     * Authorization: Required (ownership validated)
     * 
     * @param jobId the job UUID
     * @param employerId the employer ID
     * @return 204 No Content on success, 400 Bad Request if published
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable("id") UUID jobId,
            @RequestHeader("X-Employer-ID") UUID employerId) {
        jobApplicationService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }
    
    // ========================================================================
    // NESTED REQUEST DTOs (for state transitions with optional reason)
    // ========================================================================
    
    /**
     * Hold Job Request (with optional reason)
     */
    public static class HoldJobRequest {
        private String reason;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
    
    /**
     * Close Job Request (with optional reason)
     */
    public static class CloseJobRequest {
        private String reason;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
