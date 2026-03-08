package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: First Name
 * Candidate's first name with validation.
 * 
 * @author GitHub Copilot / TASK-014
 */
public class FirstName {
    private final String value;
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 50;
    
    public static FirstName of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("First name must be between %d and %d characters", 
                    MIN_LENGTH, MAX_LENGTH)
            );
        }
        
        return new FirstName(trimmed);
    }
    
    private FirstName(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirstName)) return false;
        FirstName that = (FirstName) o;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return "FirstName{" + value + '}';
    }
}
