package com.jrecruiter.userservice.application.dtos;

import java.time.LocalDateTime;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Application.ApplicationStatus;

public class ApplicationResponse {
    private UUID applicationId;
    private UUID candidateId;
    private UUID jobId;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime submittedAt;
    private LocalDateTime statusChangedAt;
    private String rejectionReason;

    public ApplicationResponse() {}

    // Getters/Setters
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getCandidateId() { return candidateId; }
    public void setCandidateId(UUID candidateId) { this.candidateId = candidateId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getStatusChangedAt() { return statusChangedAt; }
    public void setStatusChangedAt(LocalDateTime statusChangedAt) { this.statusChangedAt = statusChangedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
