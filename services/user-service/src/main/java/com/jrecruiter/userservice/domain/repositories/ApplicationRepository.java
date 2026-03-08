package com.jrecruiter.userservice.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Application;

/**
 * Repository Port: ApplicationRepository
 * 
 * @author GitHub Copilot / TASK-014
 */
public interface ApplicationRepository {
    Application save(Application application);
    Optional<Application> findById(UUID applicationId);
    List<Application> findByCandidateId(UUID candidateId);
    List<Application> findByJobId(UUID jobId);
    List<Application> findByStatus(Application.ApplicationStatus status);
    boolean existsById(UUID applicationId);
    void deleteById(UUID applicationId);
    long count();
    long countByCandidateId(UUID candidateId);
    long countByJobId(UUID jobId);
}
