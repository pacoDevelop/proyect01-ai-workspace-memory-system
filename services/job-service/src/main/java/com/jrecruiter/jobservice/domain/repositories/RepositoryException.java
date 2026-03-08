package com.jrecruiter.jobservice.domain.repositories;

/**
 * Repository Exception
 * 
 * Thrown when persistence operations fail at the domain layer.
 * Domain-level translation of infrastructure errors (SQL, connection, etc.)
 * 
 * This is a pure domain exception with no infrastructure dependencies.
 * 
 * @author GitHub Copilot / TASK-008
 */
public class RepositoryException extends RuntimeException {
    
    /**
     * Constructor with message only
     */
    public RepositoryException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with cause only
     */
    public RepositoryException(Throwable cause) {
        super("Repository operation failed", cause);
    }
}
