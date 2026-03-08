package com.jrecruiter.jobservice.domain.valueobjects;

/**
 * Offered By Enum
 * 
 * Represents who is offering the job position.
 * 
 * @author GitHub Copilot / TASK-007
 */
public enum OfferedBy {
    EMPLOYER("Posted directly by the employer/company"),
    RECRUITER("Posted by a recruiter or staffing agency");
    
    private final String description;
    
    OfferedBy(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
