package com.jrecruiter.jobservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Company Name
 * 
 * Represents the employer/company name.
 * Immutable and self-validating.
 * 
 * @author GitHub Copilot / TASK-007
 */
public record CompanyName(String value) {
    
    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 100;
    
    /**
     * Creates a new CompanyName with validation.
     * 
     * @param value the company name
     * @throws IllegalArgumentException if validation fails
     */
    public CompanyName {
        Objects.requireNonNull(value, "Company name cannot be null");
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Company name must be at least " + MIN_LENGTH + " characters"
            );
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Company name must not exceed " + MAX_LENGTH + " characters"
            );
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Factory method to create a CompanyName from a string value.
     * 
     * @param value the company name
     * @return a new CompanyName instance
     */
    public static CompanyName of(String value) {
        return new CompanyName(value);
    }
}
