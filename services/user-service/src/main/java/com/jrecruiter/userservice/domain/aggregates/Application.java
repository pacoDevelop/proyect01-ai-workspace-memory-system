package com.jrecruiter.userservice.domain.aggregates;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate Root: Application (Job Application)
 * 
 * Represents a candidate's application to a job posting.
 * Bridges Candidate and Job contexts.
 * 
 * Status States:
 * - DRAFT: Application started but not submitted
 * - SUBMITTED: Sent to employer
 * - UNDER_REVIEW: Employer reviewing
 * - INTERVIEW: Candidate invited to interview
 * - REJECTED: Application rejected
 * - ACCEPTED: Application accepted / job offered
 * - WITHDRAWN: Candidate withdrew
 * 
 * @author GitHub Copilot / TASK-014
 */
public class Application {
    
    private final UUID applicationId;
    private final UUID candidateId;
    private final UUID jobId;
    private final LocalDateTime submittedAt;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime statusChangedAt;
    private String rejectionReason;
    
    public enum ApplicationStatus {
        DRAFT, SUBMITTED, UNDER_REVIEW, INTERVIEW, REJECTED, ACCEPTED, WITHDRAWN
    }
    
    /**
     * Factory method: Create new application
     */
    public static Application createApplication(UUID candidateId, UUID jobId, String coverLetter) {
        if (candidateId == null) throw new IllegalArgumentException("Candidate ID required");
        if (jobId == null) throw new IllegalArgumentException("Job ID required");
        
        return new Application(
            UUID.randomUUID(),
            candidateId,
            jobId,
            coverLetter == null ? "" : coverLetter,
            LocalDateTime.now()
        );
    }
    
    private Application(UUID applicationId, UUID candidateId, UUID jobId, 
                       String coverLetter, LocalDateTime submittedAt) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.coverLetter = coverLetter;
        this.submittedAt = submittedAt;
        this.status = ApplicationStatus.DRAFT;
        this.statusChangedAt = submittedAt;
    }
    
    /**
     * Submit application to employer
     */
    public void submit() {
        if (status != ApplicationStatus.DRAFT) {
            throw new IllegalStateException("Can only submit DRAFT applications");
        }
        this.status = ApplicationStatus.SUBMITTED;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Employer puts application under review
     */
    public void reviewApplication() {
        if (status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Can only review SUBMITTED applications");
        }
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Invite candidate to interview
     */
    public void inviteToInterview() {
        if (status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only invite from UNDER_REVIEW status");
        }
        this.status = ApplicationStatus.INTERVIEW;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Reject application
     */
    public void reject(String reason) {
        if (status == ApplicationStatus.REJECTED || status == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException(
                String.format("Cannot reject %s application", status)
            );
        }
        this.status = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Accept application (offer job)
     */
    public void accept() {
        if (status != ApplicationStatus.INTERVIEW && status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only accept from INTERVIEW or UNDER_REVIEW status");
        }
        this.status = ApplicationStatus.ACCEPTED;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Candidate withdraws application
     */
    public void withdraw() {
        if (status == ApplicationStatus.WITHDRAWN || status == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot withdraw already terminated application");
        }
        this.status = ApplicationStatus.WITHDRAWN;
        this.statusChangedAt = LocalDateTime.now();
    }
    
    /**
     * Update cover letter (DRAFT only)
     */
    public void updateCoverLetter(String coverLetter) {
        if (status != ApplicationStatus.DRAFT) {
            throw new IllegalStateException("Can only update DRAFT applications");
        }
        this.coverLetter = coverLetter == null ? "" : coverLetter;
    }
    
    // Accessors
    public UUID getApplicationId() { return applicationId; }
    public UUID getCandidateId() { return candidateId; }
    public UUID getJobId() { return jobId; }
    public ApplicationStatus getStatus() { return status; }
    public String getCoverLetter() { return coverLetter; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getStatusChangedAt() { return statusChangedAt; }
    public String getRejectionReason() { return rejectionReason; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application)) return false;
        Application that = (Application) o;
        return Objects.equals(applicationId, that.applicationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "applicationId=" + applicationId +
                ", candidateId=" + candidateId +
                ", jobId=" + jobId +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                '}';
    }
}
