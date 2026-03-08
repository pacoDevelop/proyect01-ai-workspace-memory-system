package com.jrecruiter.userservice.domain.aggregates;

import java.time.LocalDateTime;
import java.util.*;
import com.jrecruiter.userservice.domain.valueobjects.*;
import com.jrecruiter.userservice.domain.events.*;

/**
 * Aggregate Root: Employer
 * 
 * Represents an employer account with all employer profile information.
 * Follows DDD principles with value objects and domain events.
 * 
 * Status Transitions:
 * - PENDING_VERIFICATION → ACTIVE (email verified)
 * - PENDING_VERIFICATION → INACTIVE (registration cancelled)
 * - ACTIVE → SUSPENDED (policy violation, payment issue)
 * - ACTIVE → INACTIVE (voluntary closure)
 * - SUSPENDED → ACTIVE (issue resolved)
 * - SUSPENDED → INACTIVE (permanent closure while suspended)
 * 
 * @author GitHub Copilot / TASK-013
 */
public class Employer {
    
    // Surrogate Key
    private final UUID employerId;
    
    // Value Objects
    private final EmployerName employerName;
    private final Email email;
    private final CompanyRegistration companyRegistration;
    private final PhoneNumber phoneNumber;
    
    // State
    private EmployerStatus status;
    
    // Timestamps
    private final LocalDateTime registeredAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime deactivatedAt;
    
    // Domain Events
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * Factory method: Register new employer
     * Creates employer in PENDING_VERIFICATION status.
     */
    public static Employer registerEmployer(
            EmployerName employerName,
            Email email,
            CompanyRegistration companyRegistration,
            PhoneNumber phoneNumber) {
        
        if (employerName == null || email == null || 
            companyRegistration == null || phoneNumber == null) {
            throw new IllegalArgumentException("All employer fields are required");
        }
        
        UUID employerId = UUID.randomUUID();
        Employer employer = new Employer(employerId, employerName, email, 
            companyRegistration, phoneNumber);
        
        employer.addDomainEvent(
            new EmployerEvents.EmployerRegisteredEvent(
                employerId,
                email.getValue(),
                employerName.getValue(),
                LocalDateTime.now()
            )
        );
        
        return employer;
    }
    
    /**
     * Private constructor for factory method and reconstruction.
     */
    private Employer(UUID employerId, EmployerName employerName, Email email,
                    CompanyRegistration companyRegistration, PhoneNumber phoneNumber) {
        this.employerId = employerId;
        this.employerName = employerName;
        this.email = email;
        this.companyRegistration = companyRegistration;
        this.phoneNumber = phoneNumber;
        this.status = EmployerStatus.PENDING_VERIFICATION;
        this.registeredAt = LocalDateTime.now();
    }
    
    /**
     * Verify employer email and transition to ACTIVE status.
     */
    public void verify() {
        if (!canTransitionTo(EmployerStatus.ACTIVE)) {
            throw new IllegalStateException(
                String.format("Cannot verify employer in %s status", status)
            );
        }
        
        this.status = EmployerStatus.ACTIVE;
        this.verifiedAt = LocalDateTime.now();
        
        addDomainEvent(
            new EmployerEvents.EmployerVerifiedEvent(employerId, email.getValue(), LocalDateTime.now())
        );
    }
    
    /**
     * Suspend employer account (due to policy violation, payment issue, etc).
     */
    public void suspend(String reason) {
        if (!canTransitionTo(EmployerStatus.SUSPENDED)) {
            throw new IllegalStateException(
                String.format("Cannot suspend employer in %s status", status)
            );
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Suspension reason required");
        }
        
        this.status = EmployerStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        
        addDomainEvent(
            new EmployerEvents.EmployerSuspendedEvent(employerId, email.getValue(), reason, 
                suspendedAt)
        );
    }
    
    /**
     * Reactivate suspended employer account.
     */
    public void reactivate() {
        if (status != EmployerStatus.SUSPENDED) {
            throw new IllegalStateException(
                String.format("Can only reactivate from SUSPENDED status, current: %s", 
                    status)
            );
        }
        
        this.status = EmployerStatus.ACTIVE;
        this.suspendedAt = null; // Clear suspension time
        
        addDomainEvent(
            new EmployerEvents.EmployerReactivatedEvent(employerId, email.getValue(), 
                LocalDateTime.now())
        );
    }
    
    /**
     * Deactivate employer (permanent closure).
     */
    public void deactivate(String reason) {
        if (status == EmployerStatus.INACTIVE) {
            throw new IllegalStateException("Employer already inactive");
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Deactivation reason required");
        }
        
        this.status = EmployerStatus.INACTIVE;
        this.deactivatedAt = LocalDateTime.now();
        
        addDomainEvent(
            new EmployerEvents.EmployerDeactivatedEvent(employerId, email.getValue(), reason, 
                deactivatedAt)
        );
    }
    
    /**
     * Can transition to target status from current status?
     */
    private boolean canTransitionTo(EmployerStatus target) {
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
    public UUID getEmployerId() {
        return employerId;
    }
    
    public EmployerName getEmployerName() {
        return employerName;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public CompanyRegistration getCompanyRegistration() {
        return companyRegistration;
    }
    
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    
    public EmployerStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
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
        if (!(o instanceof Employer employer)) return false;
        return Objects.equals(employerId, employer.employerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(employerId);
    }
    
    @Override
    public String toString() {
        return "Employer{" +
                "employerId=" + employerId +
                ", employerName=" + employerName +
                ", email=" + email +
                ", status=" + status +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
