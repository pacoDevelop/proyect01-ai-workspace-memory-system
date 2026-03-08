package com.jrecruiter.userservice.domain.exceptions;

/**
 * Domain Exception: Invalid Employer
 * Thrown when employer data violates business rules.
 * 
 * @author GitHub Copilot / TASK-013
 */
public class InvalidEmployerException extends RuntimeException {
    
    public InvalidEmployerException(String message) {
        super(message);
    }
    
    public InvalidEmployerException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Domain Exception: Invalid Employer State
 * Thrown when attempting invalid state transitions.
 * 
 * @author GitHub Copilot / TASK-013
 */
class InvalidEmployerStateException extends RuntimeException {
    
    public InvalidEmployerStateException(String message) {
        super(message);
    }
    
    public InvalidEmployerStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
