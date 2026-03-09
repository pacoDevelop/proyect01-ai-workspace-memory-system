package com.jrecruiter.jobservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Job Title
 * 
 * Represents the job position title/name.
 * Immutable and self-validating.
 * 
 * @author GitHub Copilot / TASK-007
 */
public record JobTitle(String value) {
    
    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 100;
    
    /**
     * Creates a new JobTitle with validation.
     * 
     * @param value the job title text
     * @throws IllegalArgumentException if validation fails
     */
    public JobTitle {
        Objects.requireNonNull(value, "Job title cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Job title must be at least " + MIN_LENGTH + " characters (got: " + trimmed.length() + ")"
            );
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Job title must not exceed " + MAX_LENGTH + " characters (got: " + trimmed.length() + ")"
            );
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Factory method to create a JobTitle from a string value.
     * 
     * @param value the job title text
     * @return a new JobTitle instance
     */
    public static JobTitle of(String value) {
        return new JobTitle(value);
    }
}
