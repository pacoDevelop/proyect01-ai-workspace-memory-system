package com.jrecruiter.jobservice.domain.exceptions;

/**
 * Exception thrown when a state transition is invalid.
 * 
 * Examples:
 * - Cannot publish job that is already closed
 * - Cannot close job that is not published
 * - Invalid state transition attempted
 * 
 * @author GitHub Copilot / TASK-007
 */
public class InvalidJobStateException extends JobDomainException {
    
    public InvalidJobStateException(String message) {
        super(message);
    }
    
    public InvalidJobStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
