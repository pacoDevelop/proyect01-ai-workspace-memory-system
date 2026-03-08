package com.jrecruiter.userservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Phone Number
 * 
 * International format phone number (E.164 or +country-number).
 * Basic validation only, format not strictly enforced per region.
 * 
 * @author GitHub Copilot / TASK-013
 */
public class PhoneNumber {
    
    private final String value;
    
    private PhoneNumber(String value) {
        this.value = value;
    }
    
    /**
     * Factory method
     */
    public static PhoneNumber of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        
        String trimmed = value.trim();
        
        // Basic validation: starts with + and contains only digits and hyphens
        if (!trimmed.matches("^\\+?[0-9\\-\\s]{6,20}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        return new PhoneNumber(trimmed);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "PhoneNumber{" + value + '}';
    }
}
