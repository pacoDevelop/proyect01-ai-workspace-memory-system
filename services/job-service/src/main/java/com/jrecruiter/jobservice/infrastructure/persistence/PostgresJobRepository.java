package com.jrecruiter.jobservice.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jrecruiter.jobservice.domain.aggregates.Job;
import com.jrecruiter.jobservice.domain.repositories.JobRepository;
import com.jrecruiter.jobservice.domain.repositories.RepositoryException;
import com.jrecruiter.jobservice.domain.valueobjects.CompanyName;
import com.jrecruiter.jobservice.domain.valueobjects.JobDescription;
import com.jrecruiter.jobservice.domain.valueobjects.JobLocation;
import com.jrecruiter.jobservice.domain.valueobjects.JobPostingStatus;
import com.jrecruiter.jobservice.domain.valueobjects.JobSalary;
import com.jrecruiter.jobservice.domain.valueobjects.JobTitle;
import com.jrecruiter.jobservice.domain.valueobjects.OfferedBy;

/**
 * PostgreSQL JPA Adapter for JobRepository
 * 
 * ADAPTER pattern: Implements the domain port using Spring Data JPA.
 * Hexagonal Architecture: This is the ADAPTER that implements the PORT.
 * 
 * Translates between domain aggregates and JPA entities.
 * Manages persistence operations and transactions.
 * 
 * Key responsibilities:
 * - Map Job (domain) ↔ JobJpaEntity (persistence)
 * - Handle database transactions
 * - Implement repository queries
 * - Translate infrastructure exceptions to domain exceptions
 * 
 * @author GitHub Copilot / TASK-009
 */
@Repository
@Transactional
public class PostgresJobRepository implements JobRepository {
    
    private final JobJpaSpringDataRepository springDataRepository;
    
    public PostgresJobRepository(JobJpaSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }
    
    // ========================================================================
    // PERSISTENCE OPERATIONS
    // ========================================================================
    
    @Override
    public Job save(Job job) throws RepositoryException {
        try {
            JobJpaEntity entity = toPersistence(job);
            JobJpaEntity saved = springDataRepository.save(entity);
            // Clear domain events after successful persistence
            job.clearDomainEvents();
            return toDomain(saved);
        } catch (Exception e) {
            throw new RepositoryException("Failed to save job: " + job.getJobId(), e);
        }
    }
    
    @Override
    public void delete(UUID jobId) throws RepositoryException {
        try {
            springDataRepository.deleteById(jobId);
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete job: " + jobId, e);
        }
    }
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - Primary Keys
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findById(UUID jobId) throws RepositoryException {
        try {
            return springDataRepository.findById(jobId)
                    .map(this::toDomain);
        } catch (Exception e) {
            throw new RepositoryException("Failed to find job by ID: " + jobId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findByUniversalId(String universalId) throws RepositoryException {
        try {
            return springDataRepository.findByUniversalId(universalId)
                    .map(this::toDomain);
        } catch (Exception e) {
            throw new RepositoryException("Failed to find job by universal ID: " + universalId, e);
        }
    }
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - By Employer
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> findByEmployerId(UUID employerId) throws RepositoryException {
        try {
            return springDataRepository.findByEmployerId(employerId)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Failed to find jobs by employer ID: " + employerId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> findPublishedByEmployerIdWithPagination(UUID employerId, int offset, int limit) 
            throws RepositoryException {
        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            return springDataRepository.findPublishedByEmployerIdWithPagination(employerId, pageable)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Failed to find published jobs by employer ID: " + employerId, e);
        }
    }
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - By Status
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> findByStatus(JobPostingStatus status) throws RepositoryException {
        try {
            return springDataRepository.findByStatus(status)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Failed to find jobs by status: " + status, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> findPublishedWithPagination(int offset, int limit) throws RepositoryException {
        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            return springDataRepository.findPublishedWithPagination(pageable)
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Failed to find published jobs with pagination", e);
        }
    }
    
    // ========================================================================
    // RETRIEVAL OPERATIONS - Counts
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public long countByEmployerId(UUID employerId) throws RepositoryException {
        try {
            return springDataRepository.countByEmployerId(employerId);
        } catch (Exception e) {
            throw new RepositoryException("Failed to count jobs by employer ID: " + employerId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByStatus(JobPostingStatus status) throws RepositoryException {
        try {
            return springDataRepository.countByStatus(status);
        } catch (Exception e) {
            throw new RepositoryException("Failed to count jobs by status: " + status, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countAll() throws RepositoryException {
        try {
            return springDataRepository.count();
        } catch (Exception e) {
            throw new RepositoryException("Failed to count all jobs", e);
        }
    }
    
    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> findAll() throws RepositoryException {
        try {
            return springDataRepository.findAll()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Failed to find all jobs", e);
        }
    }
    
    @Override
    public void deleteAll() throws RepositoryException {
        try {
            springDataRepository.deleteAll();
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete all jobs", e);
        }
    }
    
    // ========================================================================
    // TRANSACTION-LIKE OPERATIONS
    // ========================================================================
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID jobId) throws RepositoryException {
        try {
            return springDataRepository.existsById(jobId);
        } catch (Exception e) {
            throw new RepositoryException("Failed to check if job exists: " + jobId, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUpdate(UUID jobId, long expectedVersion) throws RepositoryException {
        try {
            Optional<JobJpaEntity> entity = springDataRepository.findById(jobId);
            if (entity.isEmpty()) {
                return false;
            }
            // Compare versions for optimistic locking
            return entity.get().getVersion() == expectedVersion;
        } catch (Exception e) {
            throw new RepositoryException("Failed to check if job can be updated: " + jobId, e);
        }
    }
    
    // ========================================================================
    // MAPPING: Domain ↔ Persistence
    // ========================================================================
    
    /**
     * Map from domain Job aggregate to JPA entity
     */
    private JobJpaEntity toPersistence(Job job) {
        JobLocationEmbeddable locationEmbeddable = new JobLocationEmbeddable(
                job.getLocation().getStreet(),
                job.getLocation().getCity(),
                job.getLocation().getStateProvince(),
                job.getLocation().getPostalCode(),
                job.getLocation().getCountry(),
                job.getLocation().getCountryCode(),
                job.getLocation().getLatitude(),
                job.getLocation().getLongitude(),
                job.getLocation().isRemote()
        );
        
        JobSalaryEmbeddable salaryEmbeddable = new JobSalaryEmbeddable(
                job.getSalary().getMinAmount(),
                job.getSalary().getMaxAmount(),
                job.getSalary().getCurrency().name(),
                job.getSalary().getFrequency().name()
        );
        
        return new JobJpaEntity(
                job.getJobId(),
                job.getUniversalId(),
                job.getEmployerId(),
                job.getIndustryId(),
                job.getRegionId(),
                job.getTitle().getValue(),
                job.getDescription().getValue(),
                job.getCompanyName().getValue(),
                locationEmbeddable,
                salaryEmbeddable,
                job.getOfferedBy().name(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getPublishedAt(),
                job.getClosedAt(),
                job.getUpdatedAt()
        );
    }
    
    /**
     * Map from JPA entity to domain Job aggregate
     */
    private Job toDomain(JobJpaEntity entity) {
        JobLocation location = JobLocation.withAddress(
                entity.getLocation().getStreet(),
                entity.getLocation().getCity(),
                entity.getLocation().getStateProvince(),
                entity.getLocation().getPostalCode(),
                entity.getLocation().getCountry(),
                entity.getLocation().getCountryCode(),
                entity.getLocation().isRemote()
        );
        
        JobSalary salary = JobSalary.of(
                entity.getSalary().getMinAmount(),
                entity.getSalary().getMaxAmount(),
                entity.getSalary().getCurrency(),
                entity.getSalary().getFrequency()
        );
        
        return Job.reconstruct(
                entity.getJobId(),
                entity.getUniversalId(),
                entity.getEmployerId(),
                entity.getIndustryId(),
                entity.getRegionId(),
                JobTitle.of(entity.getTitle()),
                JobDescription.of(entity.getDescription()),
                CompanyName.of(entity.getCompanyName()),
                location,
                salary,
                OfferedBy.valueOf(entity.getOfferedBy()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPublishedAt(),
                entity.getClosedAt(),
                entity.getUpdatedAt()
        );
    }
}
