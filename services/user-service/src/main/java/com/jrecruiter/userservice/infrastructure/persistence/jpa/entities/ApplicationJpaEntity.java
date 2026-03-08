package com.jrecruiter.userservice.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Application.ApplicationStatus;

@Entity
@Table(name = "applications")
public class ApplicationJpaEntity {

    @Id
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    public ApplicationJpaEntity() {}

    // Getters and Setters
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

    public static ApplicationJpaEntityBuilder builder() {
        return new ApplicationJpaEntityBuilder();
    }

    public static class ApplicationJpaEntityBuilder {
        private final ApplicationJpaEntity entity = new ApplicationJpaEntity();
        public ApplicationJpaEntityBuilder applicationId(UUID id) { entity.setApplicationId(id); return this; }
        public ApplicationJpaEntityBuilder candidateId(UUID id) { entity.setCandidateId(id); return this; }
        public ApplicationJpaEntityBuilder jobId(UUID id) { entity.setJobId(id); return this; }
        public ApplicationJpaEntityBuilder status(ApplicationStatus status) { entity.setStatus(status); return this; }
        public ApplicationJpaEntityBuilder coverLetter(String cl) { entity.setCoverLetter(cl); return this; }
        public ApplicationJpaEntityBuilder submittedAt(LocalDateTime dt) { entity.setSubmittedAt(dt); return this; }
        public ApplicationJpaEntityBuilder statusChangedAt(LocalDateTime dt) { entity.setStatusChangedAt(dt); return this; }
        public ApplicationJpaEntityBuilder rejectionReason(String reason) { entity.setRejectionReason(reason); return this; }
        public ApplicationJpaEntity build() { return entity; }
    }
}
