package com.jrecruiter.userservice.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Candidate Domain Events
 * 
 * @author GitHub Copilot / TASK-014
 */

record CandidateRegisteredEvent(
        UUID candidateId,
        String email,
        String firstName,
        String lastName,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

record CandidateProfileCompletedEvent(
        UUID candidateId,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

record CandidateProfileUpdatedEvent(
        UUID candidateId,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

record CandidateSuspendedEvent(
        UUID candidateId,
        String email,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

record CandidateReactivatedEvent(
        UUID candidateId,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

record CandidateDeactivatedEvent(
        UUID candidateId,
        String email,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
