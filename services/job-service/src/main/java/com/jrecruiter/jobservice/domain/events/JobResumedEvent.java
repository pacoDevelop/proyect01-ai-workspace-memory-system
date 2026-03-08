package com.jrecruiter.jobservice.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Job Resumed
 * 
 * Emitted when a job is transitioned back to PUBLISHED from ON_HOLD.
 * Job becomes visible to candidates again.
 * 
 * @author GitHub Copilot / TASK-007
 */
public class JobResumedEvent extends JobDomainEvent {
    
    /**
     * Create a JobResumedEvent with current timestamp.
     */
    public JobResumedEvent(UUID jobId) {
        super(jobId);
    }
    
    /**
     * Create a JobResumedEvent with specific timestamp (for event sourcing).
     */
    public JobResumedEvent(UUID eventId, UUID jobId, Instant occurredAt) {
        super(eventId, jobId, occurredAt);
    }
    
    @Override
    public String getEventType() {
        return "JobResumed";
    }
}
