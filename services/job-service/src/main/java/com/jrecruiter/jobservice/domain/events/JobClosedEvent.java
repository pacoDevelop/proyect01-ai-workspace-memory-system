package com.jrecruiter.jobservice.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Job Closed
 * 
 * Emitted when a job is transitioned to CLOSED status.
 * Indicates the job is no longer accepting applications.
 * 
 * @author GitHub Copilot / TASK-007
 */
public class JobClosedEvent extends JobDomainEvent {
    
    private final String reason;  // Optional: why job was closed
    
    /**
     * Create a JobClosedEvent with current timestamp.
     */
    public JobClosedEvent(UUID jobId, String reason) {
        super(jobId);
        this.reason = reason;
    }
    
    /**
     * Create a JobClosedEvent with specific timestamp (for event sourcing).
     */
    public JobClosedEvent(UUID eventId, UUID jobId, Instant occurredAt, String reason) {
        super(eventId, jobId, occurredAt);
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "JobClosed";
    }
    
    public String getReason() {
        return reason;
    }
}
