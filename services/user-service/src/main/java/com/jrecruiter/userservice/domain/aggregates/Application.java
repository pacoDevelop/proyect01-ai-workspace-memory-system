package com.jrecruiter.userservice.domain.aggregates;

import java.time.LocalDateTime;
import java.util.*;
import com.jrecruiter.userservice.domain.events.*;

/**
 * Aggregate Root: Application (Job Application)
 * 
 * Represents a candidate's application to a job posting.
 */
public class Application {
    
    public enum ApplicationStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        INTERVIEW,
        OFFERED,
        ACCEPTED,
        REJECTED,
        WITHDRAWN
    }

    private final UUID applicationId;
    private final UUID candidateId;
    private final UUID jobId;
    
    private String coverLetter;
    private ApplicationStatus status;
    private String rejectionReason;
    
    private LocalDateTime submittedAt;
    private LocalDateTime statusChangedAt;
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Application submit(UUID appId, UUID candidateId, UUID jobId, String coverLetter) {
        Application app = new Application(appId, candidateId, jobId, coverLetter);
        app.status = ApplicationStatus.SUBMITTED;
        app.submittedAt = LocalDateTime.now();
        app.statusChangedAt = app.submittedAt;
        
        app.addDomainEvent(
            new ApplicationEvents.ApplicationSubmittedEvent(
                appId, candidateId, jobId, app.submittedAt
            )
        );
        return app;
    }

    private Application(UUID applicationId, UUID candidateId, UUID jobId, String coverLetter) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.coverLetter = coverLetter;
        this.status = ApplicationStatus.DRAFT;
    }

    public void review() {
        if (status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Can only review submitted applications");
        }
        transitionTo(ApplicationStatus.UNDER_REVIEW, null);
    }

    public void scheduleInterview() {
        if (status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only schedule interview from UNDER_REVIEW");
        }
        transitionTo(ApplicationStatus.INTERVIEW, null);
    }

    public void reject(String reason) {
        if (status == ApplicationStatus.ACCEPTED || status == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot reject terminated application");
        }
        this.rejectionReason = reason;
        transitionTo(ApplicationStatus.REJECTED, reason);
    }

    public void accept() {
        if (status != ApplicationStatus.INTERVIEW && status != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only accept from INTERVIEW or UNDER_REVIEW");
        }
        transitionTo(ApplicationStatus.ACCEPTED, null);
    }

    public void withdraw() {
        if (status == ApplicationStatus.WITHDRAWN || status == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot withdraw terminated application");
        }
        
        LocalDateTime now = LocalDateTime.now();
        addDomainEvent(new ApplicationEvents.ApplicationWithdrawnEvent(applicationId, candidateId, now));
        transitionTo(ApplicationStatus.WITHDRAWN, "Withdrawn by candidate");
    }

    private void transitionTo(ApplicationStatus nextStatus, String reason) {
        ApplicationStatus oldStatus = this.status;
        this.status = nextStatus;
        this.statusChangedAt = LocalDateTime.now();
        
        addDomainEvent(
            new ApplicationEvents.ApplicationStatusChangedEvent(
                applicationId, oldStatus, nextStatus, reason, statusChangedAt
            )
        );
    }

    public void updateCoverLetter(String coverLetter) {
        if (status != ApplicationStatus.DRAFT) {
            throw new IllegalStateException("Can only update DRAFT applications");
        }
        this.coverLetter = coverLetter == null ? "" : coverLetter;
    }

    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // Reconstruction method
    public static Application reconstruct(UUID applicationId, UUID candidateId, UUID jobId, 
                                        String coverLetter, ApplicationStatus status, 
                                        String rejectionReason, LocalDateTime submittedAt, 
                                        LocalDateTime statusChangedAt) {
        Application app = new Application(applicationId, candidateId, jobId, coverLetter);
        app.status = status;
        app.rejectionReason = rejectionReason;
        app.submittedAt = submittedAt;
        app.statusChangedAt = statusChangedAt;
        return app;
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
}
