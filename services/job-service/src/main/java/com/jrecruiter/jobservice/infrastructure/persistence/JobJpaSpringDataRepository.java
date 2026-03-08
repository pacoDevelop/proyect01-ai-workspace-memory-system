package com.jrecruiter.jobservice.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;

/**
 * Spring Data JPA Repository for Job entities
 * 
 * Internal infrastructure interface - NOT part of the domain model.
 * Provides technical queries for the PostgreSQL adapter.
 * 
 * This is purely infrastructure layer and should never be injected into domain services.
 * 
 * @author GitHub Copilot / TASK-009
 */
@Repository
public interface JobJpaSpringDataRepository extends JpaRepository<JobJpaEntity, UUID> {
    
    /**
     * Find by universal ID (business key)
     */
    Optional<JobJpaEntity> findByUniversalId(String universalId);
    
    /**
     * Find all jobs by employer ID
     */
    List<JobJpaEntity> findByEmployerId(UUID employerId);
    
    /**
     * Find published jobs by employer with pagination
     */
    @Query("SELECT j FROM JobJpaEntity j WHERE j.employerId = :employerId AND j.status = 'PUBLISHED' ORDER BY j.publishedAt DESC")
    List<JobJpaEntity> findPublishedByEmployerIdWithPagination(
            @Param("employerId") UUID employerId,
            org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find all jobs by status
     */
    List<JobJpaEntity> findByStatus(JobPostingStatus status);
    
    /**
     * Find published jobs with pagination
     */
    @Query("SELECT j FROM JobJpaEntity j WHERE j.status = 'PUBLISHED' ORDER BY j.publishedAt DESC")
    List<JobJpaEntity> findPublishedWithPagination(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count jobs by employer
     */
    long countByEmployerId(UUID employerId);
    
    /**
     * Count jobs by status
     */
    long countByStatus(JobPostingStatus status);
}
