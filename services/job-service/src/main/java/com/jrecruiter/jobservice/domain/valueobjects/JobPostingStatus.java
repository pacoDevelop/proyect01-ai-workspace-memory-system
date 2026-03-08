package com.jrecruiter.jobservice.domain.valueobjects;

/**
 * Job Posting Status Enum
 * 
 * Represents the lifecycle status of a job posting.
 * 
 * State transitions:
 * - DRAFT → PUBLISHED (when all validation passed)
 * - PUBLISHED → CLOSED (when done accepting applications)
 * - PUBLISHED → ON_HOLD (temporary pause)
 * - ON_HOLD → PUBLISHED (resume)
 * - CLOSED → ARCHIVED (cleanup)
 * 
 * @author GitHub Copilot / TASK-007
 */
public enum JobPostingStatus {
    DRAFT("Job posting is in draft status, not yet visible"),
    PUBLISHED("Job posting is published and visible to candidates"),
    ON_HOLD("Job posting is temporarily on hold"),
    CLOSED("Job posting is closed, no longer accepting applications"),
    ARCHIVED("Job posting is archived for historical reference");
    
    private final String description;
    
    JobPostingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if transition is allowed from current status to target status.
     */
    public boolean canTransitionTo(JobPostingStatus target) {
        if (this == target) {
            return false; // Same status, no transition
        }
        
        return switch (this) {
            case DRAFT -> target == JobPostingStatus.PUBLISHED;
            case PUBLISHED -> target == JobPostingStatus.CLOSED || target == JobPostingStatus.ON_HOLD;
            case ON_HOLD -> target == JobPostingStatus.PUBLISHED;
            case CLOSED -> target == JobPostingStatus.ARCHIVED;
            case ARCHIVED -> false; // Final state
        };
    }
}
