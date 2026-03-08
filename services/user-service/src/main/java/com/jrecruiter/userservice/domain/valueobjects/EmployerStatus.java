package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: Employer Status (Enum)
 * 
 * Status of employer account.
 * - PENDING_VERIFICATION: New registration, awaiting email verification
 * - ACTIVE: Verified and in good standing
 * - SUSPENDED: Temporarily disabled (payment, policy violation)
 * - INACTIVE: Permanently disabled or closed
 * 
 * @author GitHub Copilot / TASK-013
 */
public enum EmployerStatus {
    PENDING_VERIFICATION("Awaiting email verification"),
    ACTIVE("Active employer account"),
    SUSPENDED("Account suspended"),
    INACTIVE("Account inactive or closed");
    
    private final String description;
    
    EmployerStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Can transition from current status to target status?
     */
    public boolean canTransitionTo(EmployerStatus target) {
        if (this == target) {
            return false; // No self-transition
        }
        
        return switch (this) {
            case PENDING_VERIFICATION -> target == ACTIVE || target == INACTIVE;
            case ACTIVE -> target == SUSPENDED || target == INACTIVE;
            case SUSPENDED -> target == ACTIVE || target == INACTIVE;
            case INACTIVE -> false; // No transition from inactive
        };
    }
}
