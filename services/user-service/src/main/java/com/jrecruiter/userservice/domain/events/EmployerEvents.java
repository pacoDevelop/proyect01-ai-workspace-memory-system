package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Employer Domain Events
 */
public final class EmployerEvents {
    private EmployerEvents() {}

    public record EmployerRegisteredEvent(
            UUID employerId,
            String email,
            String companyName,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record EmployerVerifiedEvent(
            UUID employerId,
            String email,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record EmployerSuspendedEvent(
            UUID employerId,
            String email,
            String reason,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record EmployerReactivatedEvent(
            UUID employerId,
            String email,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record EmployerDeactivatedEvent(
            UUID employerId,
            String email,
            String reason,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }
}
