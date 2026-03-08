package com.jrecruiter.userservice.domain.aggregates;

import java.time.LocalDateTime;
import java.util.*;
import com.jrecruiter.userservice.domain.valueobjects.*;
import com.jrecruiter.userservice.domain.events.*;

/**
 * Aggregate Root: Candidate
 * 
 * Represents a job candidate profile with application history.
 * Parallels Employer aggregate but for candidate side.
 * 
 * Status Transitions:
 * - PENDING_COMPLETION → ACTIVE (profile completed)
 * - PENDING_COMPLETION → INACTIVE (cancelled)
 * - ACTIVE → SUSPENDED (policy violation)
 * - ACTIVE → INACTIVE (voluntary closure)
 * - SUSPENDED → ACTIVE (issue resolved)
 * - SUSPENDED → INACTIVE (permanent closure while suspended)
 * 
 * @author GitHub Copilot / TASK-014
 */
public class Candidate {
    
    // Surrogate Key
    private final UUID candidateId;
    
    // Value Objects
    private final Email email;
    private final FirstName firstName;
    private final LastName lastName;
    private final PhoneNumber phoneNumber;
    
    // Profile Information
    private final CandidateSkills skills;
    private final ExperienceLevel experienceLevel;
    private final DesiredLocation desiredLocation;
    
    // State
    private CandidateProfileStatus status;
    private String bio;
    
    // Timestamps
    private final LocalDateTime registeredAt;
    private LocalDateTime profileCompletedAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime deactivatedAt;
    
    // Domain Events
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * Factory method: Register new candidate
     * Creates candidate in PENDING_COMPLETION status.
     */
    public static Candidate registerCandidate(
            Email email,
            FirstName firstName,
            LastName lastName,
            PhoneNumber phoneNumber) {
        
        if (email == null || firstName == null || lastName == null || phoneNumber == null) {
            throw new IllegalArgumentException("All candidate fields are required");
        }
        
        UUID candidateId = UUID.randomUUID();
        Candidate candidate = new Candidate(candidateId, email, firstName, lastName, phoneNumber);
        
        candidate.addDomainEvent(
            new CandidateRegisteredEvent(
                candidateId,
                email.getValue(),
                firstName.getValue(),
                lastName.getValue(),
                LocalDateTime.now()
            )
        );
        
        return candidate;
    }
    
    /**
     * Private constructor for factory method and reconstruction.
     */
    private Candidate(UUID candidateId, Email email, FirstName firstName, 
                     LastName lastName, PhoneNumber phoneNumber) {
        this.candidateId = candidateId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.status = CandidateProfileStatus.PENDING_COMPLETION;
        this.registeredAt = LocalDateTime.now();
    }
    
    /**
     * Complete candidate profile with skills and preferences.
     */
    public void completeProfile(CandidateSkills skills, ExperienceLevel experience, 
                                DesiredLocation location, String bio) {
        if (skills == null || experience == null || location == null) {
            throw new IllegalArgumentException("Profile requires skills, experience, and location");
        }
        
        if (status != CandidateProfileStatus.PENDING_COMPLETION) {
            throw new IllegalStateException(
                String.format("Can only complete profile in PENDING_COMPLETION status, current: %s", status)
            );
        }
        
        // Use reflection-like access for assignment
        try {
            var candidateClass = Candidate.class;
            var skillsField = candidateClass.getDeclaredField("skills");
            skillsField.setAccessible(true);
            skillsField.set(this, skills);
            
            var expField = candidateClass.getDeclaredField("experienceLevel");
            expField.setAccessible(true);
            expField.set(this, experience);
            
            var locField = candidateClass.getDeclaredField("desiredLocation");
            locField.setAccessible(true);
            locField.set(this, location);
            
            var bioField = candidateClass.getDeclaredField("bio");
            bioField.setAccessible(true);
            bioField.set(this, bio == null || bio.isBlank() ? "" : bio);
        } catch (Exception e) {
            throw new RuntimeException("Error setting profile fields", e);
        }
        
        this.status = CandidateProfileStatus.ACTIVE;
        this.profileCompletedAt = LocalDateTime.now();
        
        addDomainEvent(
            new CandidateProfileCompletedEvent(candidateId, email.getValue(), LocalDateTime.now())
        );
    }
    
    /**
     * Update candidate profile information.
     */
    public void updateProfile(CandidateSkills skills, ExperienceLevel experience, 
                             DesiredLocation location, String bio) {
        if (status == CandidateProfileStatus.INACTIVE) {
            throw new IllegalStateException("Cannot update inactive profile");
        }
        
        try {
            var candidateClass = Candidate.class;
            var skillsField = candidateClass.getDeclaredField("skills");
            skillsField.setAccessible(true);
            skillsField.set(this, skills);
            
            var expField = candidateClass.getDeclaredField("experienceLevel");
            expField.setAccessible(true);
            expField.set(this, experience);
            
            var locField = candidateClass.getDeclaredField("desiredLocation");
            locField.setAccessible(true);
            locField.set(this, location);
            
            var bioField = candidateClass.getDeclaredField("bio");
            bioField.setAccessible(true);
            bioField.set(this, bio == null || bio.isBlank() ? "" : bio);
        } catch (Exception e) {
            throw new RuntimeException("Error updating profile fields", e);
        }
        
        addDomainEvent(
            new CandidateProfileUpdatedEvent(candidateId, email.getValue(), LocalDateTime.now())
        );
    }
    
    /**
     * Suspend candidate profile.
     */
    public void suspend(String reason) {
        if (!canTransitionTo(CandidateProfileStatus.SUSPENDED)) {
            throw new IllegalStateException(
                String.format("Cannot suspend candidate in %s status", status)
            );
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Suspension reason required");
        }
        
        this.status = CandidateProfileStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        
        addDomainEvent(
            new CandidateSuspendedEvent(candidateId, email.getValue(), reason, suspendedAt)
        );
    }
    
    /**
     * Reactivate suspended candidate profile.
     */
    public void reactivate() {
        if (status != CandidateProfileStatus.SUSPENDED) {
            throw new IllegalStateException(
                String.format("Can only reactivate from SUSPENDED status, current: %s", status)
            );
        }
        
        this.status = CandidateProfileStatus.ACTIVE;
        this.suspendedAt = null;
        
        addDomainEvent(
            new CandidateReactivatedEvent(candidateId, email.getValue(), LocalDateTime.now())
        );
    }
    
    /**
     * Deactivate candidate profile (permanent closure).
     */
    public void deactivate(String reason) {
        if (status == CandidateProfileStatus.INACTIVE) {
            throw new IllegalStateException("Candidate already inactive");
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Deactivation reason required");
        }
        
        this.status = CandidateProfileStatus.INACTIVE;
        this.deactivatedAt = LocalDateTime.now();
        
        addDomainEvent(
            new CandidateDeactivatedEvent(candidateId, email.getValue(), reason, deactivatedAt)
        );
    }
    
    /**
     * Can transition to target status from current status?
     */
    private boolean canTransitionTo(CandidateProfileStatus target) {
        return status.canTransitionTo(target);
    }
    
    /**
     * Domain Event Management
     */
    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    /**
     * Accessors
     */
    public UUID getCandidateId() {
        return candidateId;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public FirstName getFirstName() {
        return firstName;
    }
    
    public LastName getLastName() {
        return lastName;
    }
    
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    
    public CandidateSkills getSkills() {
        return skills;
    }
    
    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }
    
    public DesiredLocation getDesiredLocation() {
        return desiredLocation;
    }
    
    public CandidateProfileStatus getStatus() {
        return status;
    }
    
    public String getBio() {
        return bio;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public LocalDateTime getProfileCompletedAt() {
        return profileCompletedAt;
    }
    
    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }
    
    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;
        Candidate candidate = (Candidate) o;
        return Objects.equals(candidateId, candidate.candidateId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(candidateId);
    }
    
    @Override
    public String toString() {
        return "Candidate{" +
                "candidateId=" + candidateId +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", email=" + email +
                ", status=" + status +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
