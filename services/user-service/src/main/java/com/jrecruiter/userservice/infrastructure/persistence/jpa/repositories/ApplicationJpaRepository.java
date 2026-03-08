package com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.ApplicationJpaEntity;
import java.util.List;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Application.ApplicationStatus;

@Repository
public interface ApplicationJpaRepository extends JpaRepository<ApplicationJpaEntity, UUID> {
    List<ApplicationJpaEntity> findByCandidateId(UUID candidateId);
    List<ApplicationJpaEntity> findByJobId(UUID jobId);
    List<ApplicationJpaEntity> findByStatus(ApplicationStatus status);
    long countByCandidateId(UUID candidateId);
    long countByJobId(UUID jobId);
}
