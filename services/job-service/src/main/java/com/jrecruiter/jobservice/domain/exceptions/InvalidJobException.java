package com.jrecruiter.jobservice.domain.exceptions;

/**
 * Exception thrown when aggregate invariants are violated during creation.
 * 
 * Examples:
 * - Required field is null
 * - Title is too short/long
 * - Location is invalid
 * 
 * @author GitHub Copilot / TASK-007
 */
public class InvalidJobException extends JobDomainException {
    
    public InvalidJobException(String message) {
        super(message);
    }
    
    public InvalidJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
