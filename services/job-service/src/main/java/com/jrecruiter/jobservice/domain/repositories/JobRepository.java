package com.jrecruiter.jobservice.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jrecruiter.jobservice.domain.aggregates.Job;
import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;

/**
 * Repository Port - Domain Driven Design
 * 
 * Defines the contract for persisting and retrieving Job aggregates.
 * This is a pure domain interface with NO Spring annotations or dependencies.
 * Implementation lives in infrastructure/persistence layer.
 * 
 * Hexagonal Architecture: This is the PORT that the persistence adapter implements.
 * 
 * @author GitHub Copilot / TASK-008
 */
public interface JobRepository {
    
    // ========================================================================
    // PERSISTENCE OPERATIONS
    // ========================================================================
    
    /**
     * Save or update a job aggregate.
     * 
     * Persists the job and clears domain events after successful save.
     * 
     * @param job the job aggregate to save
     * @return the persisted job (may have additional fields populated by DB)
     * @throws RepositoryException if persistence fails
     */
    Job save(Job job) throws RepositoryException;
    
    /**
     * Delete a job by its ID.
     * 
     * @param jobId the job UUID to delete
     * @throws RepositoryException if deletion fails
     */
    void delete(UUID jobId) throws RepositoryException;
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - Primary Keys
    // ========================================================================
    
    /**
     * Find job by surrogate key (jobId).
     * 
     * @param jobId the job UUID
     * @return Optional containing the job, or empty if not found
     * @throws RepositoryException if query fails
     */
    Optional<Job> findById(UUID jobId) throws RepositoryException;
    
    /**
     * Find job by business key (universalId).
     * 
     * Useful for external system integration where universalId is the
     * primary identifier.
     * 
     * @param universalId the universal identifier
     * @return Optional containing the job, or empty if not found
     * @throws RepositoryException if query fails
     */
    Optional<Job> findByUniversalId(String universalId) throws RepositoryException;
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - By Employer
    // ========================================================================
    
    /**
     * Find all jobs posted by a specific employer.
     * 
     * @param employerId the employer UUID
     * @return list of jobs (never null, may be empty)
     * @throws RepositoryException if query fails
     */
    List<Job> findByEmployerId(UUID employerId) throws RepositoryException;
    
    /**
     * Find published jobs by employer (pagination).
     * 
     * @param employerId the employer UUID
     * @param offset pagination offset (0-based)
     * @param limit maximum number of results
     * @return list of published jobs by this employer
     * @throws RepositoryException if query fails
     */
    List<Job> findPublishedByEmployerIdWithPagination(UUID employerId, int offset, int limit) 
            throws RepositoryException;
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - By Status
    // ========================================================================
    
    /**
     * Find all jobs with a specific status.
     * 
     * @param status the job posting status to filter by
     * @return list of jobs with that status
     * @throws RepositoryException if query fails
     */
    List<Job> findByStatus(JobPostingStatus status) throws RepositoryException;
    
    /**
     * Find published jobs (PUBLISHED status) with pagination.
     * 
     * Used for search service indexing and job listing pages.
     * 
     * @param offset pagination offset (0-based)
     * @param limit maximum number of results
     * @return list of published jobs
     * @throws RepositoryException if query fails
     */
    List<Job> findPublishedWithPagination(int offset, int limit) throws RepositoryException;
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - Counts
    // ========================================================================
    
    /**
     * Count total jobs by employer.
     * 
     * @param employerId the employer UUID
     * @return count of jobs owned by this employer
     * @throws RepositoryException if query fails
     */
    long countByEmployerId(UUID employerId) throws RepositoryException;
    
    /**
     * Count jobs by status.
     * 
     * Useful for analytics and job board statistics.
     * 
     * @param status the status to count
     * @return count of jobs with this status
     * @throws RepositoryException if query fails
     */
    long countByStatus(JobPostingStatus status) throws RepositoryException;
    
    /**
     * Get total count of all jobs.
     * 
     * @return total number of jobs in system
     * @throws RepositoryException if query fails
     */
    long countAll() throws RepositoryException;
    
    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================
    
    /**
     * Find all jobs (no pagination - use with caution on large datasets).
     * 
     * Consider using pagination methods for production code.
     * 
     * @return all jobs in system
     * @throws RepositoryException if query fails
     */
    List<Job> findAll() throws RepositoryException;
    
    /**
     * Delete all jobs (bulk operation - typically for data cleanup/testing).
     * 
     * @throws RepositoryException if operation fails
     */
    void deleteAll() throws RepositoryException;
    
    // ========================================================================
    // TRANSACTION-LIKE OPERATIONS
    // ========================================================================
    
    /**
     * Check if a job exists by ID.
     * 
     * @param jobId the job UUID
     * @return true if job exists, false otherwise
     * @throws RepositoryException if query fails
     */
    boolean existsById(UUID jobId) throws RepositoryException;
    
    /**
     * Verify that a job can be atomically updated (optimistic locking).
     * 
     * Some implementations may check version numbers for concurrent modification detection.
     * 
     * @param jobId the job UUID
     * @param expectedVersion the expected version
     * @return true if version matches (safe to update), false if version mismatch
     * @throws RepositoryException if query fails
     */
    boolean canUpdate(UUID jobId, long expectedVersion) throws RepositoryException;
}
