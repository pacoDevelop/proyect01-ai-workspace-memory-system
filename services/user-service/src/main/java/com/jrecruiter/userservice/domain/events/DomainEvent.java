package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events.
 * Used for event sourcing and event-driven communication.
 * 
 * @author GitHub Copilot / TASK-013
 */
public interface DomainEvent {
    LocalDateTime getOccurredAt();
}
