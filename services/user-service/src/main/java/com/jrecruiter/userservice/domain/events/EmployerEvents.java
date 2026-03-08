package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Employer Registered
 * Fired when a new employer completes registration.
 * 
 * @author GitHub Copilot / TASK-013
 */
public record EmployerRegisteredEvent(
        UUID employerId,
        String email,
        String companyName,
        LocalDateTime occurredAt
) implements DomainEvent {
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

/**
 * Domain Event: Employer Verified
 * Fired when employer email is verified.
 * 
 * @author GitHub Copilot / TASK-013
 */
record EmployerVerifiedEvent(
        UUID employerId,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

/**
 * Domain Event: Employer Suspended
 * Fired when employer account is suspended.
 * 
 * @author GitHub Copilot / TASK-013
 */
record EmployerSuspendedEvent(
        UUID employerId,
        String email,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

/**
 * Domain Event: Employer Reactivated
 * Fired when suspended employer is reactivated.
 * 
 * @author GitHub Copilot / TASK-013
 */
record EmployerReactivatedEvent(
        UUID employerId,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

/**
 * Domain Event: Employer Deactivated
 * Fired when employer account is permanently closed.
 * 
 * @author GitHub Copilot / TASK-013
 */
record EmployerDeactivatedEvent(
        UUID employerId,
        String email,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
