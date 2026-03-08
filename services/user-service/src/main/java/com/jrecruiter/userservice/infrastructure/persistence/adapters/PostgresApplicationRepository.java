package com.jrecruiter.userservice.infrastructure.persistence.adapters;

import org.springframework.stereotype.Component;
import com.jrecruiter.userservice.domain.aggregates.Application;
import com.jrecruiter.userservice.domain.repositories.ApplicationRepository;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.ApplicationJpaEntity;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories.ApplicationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Adapter for Application persistence using PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class PostgresApplicationRepository implements ApplicationRepository {

    private final ApplicationJpaRepository jpaRepository;

    @Override
    public Application save(Application application) {
        ApplicationJpaEntity entity = mapToEntity(application);
        ApplicationJpaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Application> findById(UUID applicationId) {
        return jpaRepository.findById(applicationId).map(this::mapToDomain);
    }

    @Override
    public List<Application> findByCandidateId(UUID candidateId) {
        return jpaRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Application> findByJobId(UUID jobId) {
        return jpaRepository.findByJobId(jobId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Application> findByStatus(Application.ApplicationStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID applicationId) {
        return jpaRepository.existsById(applicationId);
    }

    @Override
    public void deleteById(UUID applicationId) {
        jpaRepository.deleteById(applicationId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByCandidateId(UUID candidateId) {
        return jpaRepository.countByCandidateId(candidateId);
    }

    @Override
    public long countByJobId(UUID jobId) {
        return jpaRepository.countByJobId(jobId);
    }

    // Mapping helper methods
    private ApplicationJpaEntity mapToEntity(Application app) {
        return ApplicationJpaEntity.builder()
                .applicationId(app.getApplicationId())
                .candidateId(app.getCandidateId())
                .jobId(app.getJobId())
                .status(app.getStatus())
                .coverLetter(app.getCoverLetter())
                .submittedAt(app.getSubmittedAt())
                .statusChangedAt(app.getStatusChangedAt())
                .rejectionReason(app.getRejectionReason())
                .build();
    }

    private Application mapToDomain(ApplicationJpaEntity entity) {
        return Application.reconstruct(
            entity.getApplicationId(),
            entity.getCandidateId(),
            entity.getJobId(),
            entity.getCoverLetter(), // Corrected order: coverLetter comes first
            entity.getStatus(),
            entity.getRejectionReason(),
            entity.getSubmittedAt(),
            entity.getStatusChangedAt()
        );
    }
}
