package com.jrecruiter.jobservice.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base class for all Domain Events.
 * 
 * Domain events represent things that have happened in the domain.
 * They are immutable, timestamped, and associated with an aggregate.
 * 
 * @author GitHub Copilot / TASK-007
 */
public abstract class JobDomainEvent {
    
    private final UUID eventId;
    private final UUID jobId;
    private final Instant occurredAt;
    
    /**
     * Create a domain event with auto-generated IDs and current timestamp.
     * 
     * @param jobId the aggregate root ID that produced this event
     */
    protected JobDomainEvent(UUID jobId) {
        this.eventId = UUID.randomUUID();
        this.jobId = jobId;
        this.occurredAt = Instant.now();
    }
    
    /**
     * Create a domain event with specific timestamp (for reconstruction).
     * 
     * @param eventId the event ID
     * @param jobId the aggregate root ID
     * @param occurredAt the timestamp when event occurred
     */
    protected JobDomainEvent(UUID eventId, UUID jobId, Instant occurredAt) {
        this.eventId = eventId;
        this.jobId = jobId;
        this.occurredAt = occurredAt;
    }
    
    public UUID getEventId() {
        return eventId;
    }
    
    public UUID getJobId() {
        return jobId;
    }
    
    public Instant getOccurredAt() {
        return occurredAt;
    }
    
    /**
     * Get the event type name for routing and persistence.
     */
    public abstract String getEventType();
}
