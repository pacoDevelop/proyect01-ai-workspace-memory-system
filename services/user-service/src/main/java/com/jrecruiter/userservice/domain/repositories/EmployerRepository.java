package com.jrecruiter.userservice.domain.repositories;

import java.util.Optional;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Employer;

/**
 * Repository Port: EmployerRepository
 * 
 * Hexagonal architecture port for employer persistence.
 * Implementation handled by infrastructure (Spring Data JPA, etc).
 * 
 * @author GitHub Copilot / TASK-013
 */
public interface EmployerRepository {
    
    /**
     * Save or update an employer.
     */
    Employer save(Employer employer);
    
    /**
     * Find employer by ID.
     */
    Optional<Employer> findById(UUID employerId);
    
    /**
     * Find employer by email address.
     * Emails are unique per employer.
     */
    Optional<Employer> findByEmail(String email);
    
    /**
     * Check if employer exists by ID.
     */
    boolean existsById(UUID employerId);
    
    /**
     * Delete employer by ID.
     */
    void deleteById(UUID employerId);
    
    /**
     * Count total employers.
     */
    long count();
    
    /**
     * Count employers by status.
     */
    long countByStatus(String status);
}
