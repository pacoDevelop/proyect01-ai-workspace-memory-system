package com.jrecruiter.userservice.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Email Address
 * 
 * Valid email address with RFC 5322 basic validation.
 * Immutable and comparable.
 * 
 * @author GitHub Copilot / TASK-013
 */
public class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private final String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    /**
     * Factory method with validation
     */
    public static Email of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        String trimmed = value.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + trimmed);
        }
        
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Email must not exceed 255 characters");
        }
        
        return new Email(trimmed);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "Email{" + value + '}';
    }
}
