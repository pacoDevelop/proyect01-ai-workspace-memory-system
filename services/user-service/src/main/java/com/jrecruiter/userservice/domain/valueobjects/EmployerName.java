package com.jrecruiter.userservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Employer Name
 * 
 * Represents the legal name of an employer company.
 * Immutable, validated at construction.
 * 
 * @author GitHub Copilot / TASK-013
 */
public class EmployerName {
    
    private final String value;
    
    private EmployerName(String value) {
        this.value = value;
    }
    
    /**
     * Factory method with validation
     */
    public static EmployerName of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Employer name cannot be empty");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() < 2) {
            throw new IllegalArgumentException("Employer name must be at least 2 characters");
        }
        
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Employer name must not exceed 100 characters");
        }
        
        return new EmployerName(trimmed);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployerName that = (EmployerName) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "EmployerName{" + value + '}';
    }
}
