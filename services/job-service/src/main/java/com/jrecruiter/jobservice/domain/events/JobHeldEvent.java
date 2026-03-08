package com.jrecruiter.jobservice.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Job Put On Hold
 * 
 * Emitted when a job is transitioned to ON_HOLD status.
 * Job is temporarily hidden from candidates but can be resumed.
 * 
 * @author GitHub Copilot / TASK-007
 */
public class JobHeldEvent extends JobDomainEvent {
    
    private final String reason;  // Optional: why job was held
    
    /**
     * Create a JobHeldEvent with current timestamp.
     */
    public JobHeldEvent(UUID jobId, String reason) {
        super(jobId);
        this.reason = reason;
    }
    
    /**
     * Create a JobHeldEvent with specific timestamp (for event sourcing).
     */
    public JobHeldEvent(UUID eventId, UUID jobId, Instant occurredAt, String reason) {
        super(eventId, jobId, occurredAt);
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "JobHeld";
    }
    
    public String getReason() {
        return reason;
    }
}
