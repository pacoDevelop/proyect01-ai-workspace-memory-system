package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Application.ApplicationStatus;

/**
 * Job Application Domain Events
 */
public final class ApplicationEvents {
    private ApplicationEvents() {}

    public record ApplicationSubmittedEvent(
            UUID applicationId,
            UUID candidateId,
            UUID jobId,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record ApplicationStatusChangedEvent(
            UUID applicationId,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus,
            String reason,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record ApplicationWithdrawnEvent(
            UUID applicationId,
            UUID candidateId,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }
}
