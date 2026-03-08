package com.jrecruiter.jobservice.domain.exceptions;

/**
 * Base exception for all domain-level exceptions.
 * 
 * These exceptions represent business rule violations that should never
 * happen in normal operation (checked invariants).
 * 
 * @author GitHub Copilot / TASK-007
 */
public abstract class JobDomainException extends RuntimeException {
    
    public JobDomainException(String message) {
        super(message);
    }
    
    public JobDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
