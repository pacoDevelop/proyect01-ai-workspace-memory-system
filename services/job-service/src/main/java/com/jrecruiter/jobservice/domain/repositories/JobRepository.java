package com.jrecruiter.jobservice.domain.repositories;

import java.util.Optional;
import java.util.UUID;

/**
 * Job Repository Interface
 * 
 * Defines the contract for Job persistence operations.
 * Implements the Repository pattern for DDD.
 */
public interface JobRepository {
    
    /**
     * Save a Job aggregate
     */
    void save(Object job);
    
    /**
     * Find Job by its aggregate ID
     */
    Optional<Object> findById(UUID jobId);
    
    /**
     * Delete a Job
     */
    void delete(UUID jobId);
    
    /**
     * Check if Job exists
     */
    boolean existsById(UUID jobId);
}
