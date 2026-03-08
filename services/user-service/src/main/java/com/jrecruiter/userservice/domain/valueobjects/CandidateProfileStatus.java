package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: Candidate Profile Status
 * 
 * Status of candidate account/profile.
 * - PENDING_COMPLETION: Profile not yet completed (missing fields)
 * - ACTIVE: Profile complete and searchable
 * - SUSPENDED: Temporarily disabled
 * - INACTIVE: Deactivated or closed
 * 
 * @author GitHub Copilot / TASK-014
 */
public enum CandidateProfileStatus {
    PENDING_COMPLETION("Profile incomplete, awaiting completion"),
    ACTIVE("Active candidate profile"),
    SUSPENDED("Profile suspended"),
    INACTIVE("Profile inactive or closed");
    
    private final String description;
    
    CandidateProfileStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Can transition from current status to target status?
     */
    public boolean canTransitionTo(CandidateProfileStatus target) {
        if (this == target) {
            return false; // No self-transition
        }
        
        return switch (this) {
            case PENDING_COMPLETION -> target == ACTIVE || target == INACTIVE;
            case ACTIVE -> target == SUSPENDED || target == INACTIVE;
            case SUSPENDED -> target == ACTIVE || target == INACTIVE;
            case INACTIVE -> false; // No transition from inactive
        };
    }
}
