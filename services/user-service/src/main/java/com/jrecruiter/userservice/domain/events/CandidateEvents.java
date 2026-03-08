package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Candidate Domain Events
 */
public final class CandidateEvents {
    private CandidateEvents() {}

    public record CandidateRegisteredEvent(
            UUID candidateId,
            String email,
            String firstName,
            String lastName,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record CandidateProfileCompletedEvent(
            UUID candidateId,
            String email,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record CandidateProfileUpdatedEvent(
            UUID candidateId,
            String email,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record CandidateSuspendedEvent(
            UUID candidateId,
            String email,
            String reason,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record CandidateReactivatedEvent(
            UUID candidateId,
            String email,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }

    public record CandidateDeactivatedEvent(
            UUID candidateId,
            String email,
            String reason,
            LocalDateTime occurredAt
    ) implements DomainEvent {
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }
}
