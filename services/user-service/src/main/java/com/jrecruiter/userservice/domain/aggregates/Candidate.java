package com.jrecruiter.userservice.domain.aggregates;

import java.time.LocalDateTime;
import java.util.*;
import com.jrecruiter.userservice.domain.events.*;
import com.jrecruiter.userservice.domain.valueobjects.*;

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
    
    // Identity and Personal Info
    private final Email email;
    private final FirstName firstName;
    private final LastName lastName;
    private final PhoneNumber phoneNumber;
    
    // Profile Information
    private CandidateSkills skills;
    private ExperienceLevel experienceLevel;
    private DesiredLocation desiredLocation;
    
    // State
    private CandidateProfileStatus status;
    private String bio;
    
    // Timestamps
    private LocalDateTime registeredAt;
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
            new CandidateEvents.CandidateRegisteredEvent(
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
        
        this.skills = skills;
        this.experienceLevel = experience;
        this.desiredLocation = location;
        this.bio = bio == null || bio.isBlank() ? "" : bio;
        
        this.status = CandidateProfileStatus.ACTIVE;
        this.profileCompletedAt = LocalDateTime.now();
        
        addDomainEvent(
            new CandidateEvents.CandidateProfileCompletedEvent(candidateId, email.getValue(), LocalDateTime.now())
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
        
        if (skills != null) this.skills = skills;
        if (experience != null) this.experienceLevel = experience;
        if (location != null) this.desiredLocation = location;
        if (bio != null) this.bio = bio;
        
        addDomainEvent(
            new CandidateEvents.CandidateProfileUpdatedEvent(candidateId, email.getValue(), LocalDateTime.now())
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
            new CandidateEvents.CandidateSuspendedEvent(candidateId, email.getValue(), reason, suspendedAt)
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
            new CandidateEvents.CandidateReactivatedEvent(candidateId, email.getValue(), LocalDateTime.now())
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
            new CandidateEvents.CandidateDeactivatedEvent(candidateId, email.getValue(), reason, deactivatedAt)
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
    
    /**
     * Reconstruct candidate from persistence.
     */
    public static Candidate reconstruct(
            UUID candidateId, Email email, FirstName firstName, LastName lastName,
            PhoneNumber phoneNumber, CandidateSkills skills, ExperienceLevel experience,
            DesiredLocation location, CandidateProfileStatus status, String bio,
            LocalDateTime registeredAt, LocalDateTime profileCompletedAt,
            LocalDateTime suspendedAt, LocalDateTime deactivatedAt) {
        
        Candidate candidate = new Candidate(candidateId, email, firstName, lastName, phoneNumber);
        candidate.skills = skills;
        candidate.experienceLevel = experience;
        candidate.desiredLocation = location;
        candidate.status = status;
        candidate.bio = bio;
        
        candidate.registeredAt = registeredAt;
        candidate.profileCompletedAt = profileCompletedAt;
        candidate.suspendedAt = suspendedAt;
        candidate.deactivatedAt = deactivatedAt;
        
        return candidate;
    }
}
