package com.jrecruiter.userservice.domain.exceptions;

/**
 * Employer Exceptions Wrapper
 */
public final class EmployerExceptions {
    private EmployerExceptions() {}

    public static class EmployerNotFoundException extends RuntimeException {
        public EmployerNotFoundException(String message) { super(message); }
    }

    public static class InvalidEmployerException extends RuntimeException {
        public InvalidEmployerException(String message) { super(message); }
    }
}
