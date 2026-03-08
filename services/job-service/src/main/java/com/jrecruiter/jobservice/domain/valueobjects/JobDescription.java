package com.jrecruiter.jobservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Job Description
 * 
 * Represents the detailed job description.
 * Immutable and self-validating.
 * 
 * @author GitHub Copilot / TASK-007
 */
public record JobDescription(String value) {
    
    public static final int MIN_LENGTH = 20;
    public static final int MAX_LENGTH = 10000;
    
    /**
     * Creates a new JobDescription with validation.
     * 
     * @param value the job description text
     * @throws IllegalArgumentException if validation fails
     */
    public JobDescription {
        Objects.requireNonNull(value, "Job description cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Job description must be at least " + MIN_LENGTH + " characters (got: " + trimmed.length() + ")"
            );
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Job description must not exceed " + MAX_LENGTH + " characters (got: " + trimmed.length() + ")"
            );
        }
    }
    
    @Override
    public String toString() {
        return value.substring(0, Math.min(100, value.length())) + "...";
    }
}
