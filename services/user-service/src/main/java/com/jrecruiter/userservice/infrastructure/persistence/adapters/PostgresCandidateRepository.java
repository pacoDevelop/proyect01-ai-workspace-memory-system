package com.jrecruiter.userservice.infrastructure.persistence.adapters;

import org.springframework.stereotype.Component;
import com.jrecruiter.userservice.domain.aggregates.Candidate;
import com.jrecruiter.userservice.domain.repositories.CandidateRepository;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.CandidateJpaEntity;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories.CandidateJpaRepository;
import com.jrecruiter.userservice.domain.valueobjects.*;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostgresCandidateRepository implements CandidateRepository {

    private final CandidateJpaRepository jpaRepository;

    @Override
    public Candidate save(Candidate candidate) {
        CandidateJpaEntity entity = mapToEntity(candidate);
        CandidateJpaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Candidate> findById(UUID candidateId) {
        return jpaRepository.findById(candidateId).map(this::mapToDomain);
    }

    @Override
    public Optional<Candidate> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::mapToDomain);
    }

    @Override
    public boolean existsById(UUID candidateId) {
        return jpaRepository.existsById(candidateId);
    }

    @Override
    public void deleteById(UUID candidateId) {
        jpaRepository.deleteById(candidateId);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByStatus(status);
    }

    // Mapping helper methods
    private CandidateJpaEntity mapToEntity(Candidate candidate) {
        return CandidateJpaEntity.builder()
                .candidateId(candidate.getCandidateId())
                .email(candidate.getEmail().getValue())
                .firstName(candidate.getFirstName().getValue())
                .lastName(candidate.getLastName().getValue())
                .phoneNumber(candidate.getPhoneNumber() != null ? candidate.getPhoneNumber().getValue() : null)
                .skills(candidate.getSkills() != null ? candidate.getSkills().getValue() : null)
                .experienceYears(candidate.getExperienceLevel() != null ? candidate.getExperienceLevel().getYears() : null)
                .desiredCity(candidate.getDesiredLocation() != null ? candidate.getDesiredLocation().getCity() : null)
                .desiredCountry(candidate.getDesiredLocation() != null ? candidate.getDesiredLocation().getCountry() : null)
                .remoteOk(candidate.getDesiredLocation() != null ? candidate.getDesiredLocation().isRemoteOk() : null)
                .status(candidate.getStatus())
                .bio(candidate.getBio())
                .registeredAt(candidate.getRegisteredAt())
                .profileCompletedAt(candidate.getProfileCompletedAt())
                .suspendedAt(candidate.getSuspendedAt())
                .deactivatedAt(candidate.getDeactivatedAt())
                .build();
    }

    private Candidate mapToDomain(CandidateJpaEntity entity) {
        // We use reflection or a special constructor in Candidate for reconstruction if needed,
        // but since Candidate has a private constructor and no public set methods for id/email,
        // we might need a reconstruction factory or change constructor to package-private.
        
        // For now, I'll use a hack or assume we can use a reconstruction method.
        // Actually, Candidate has a private constructor. I'll make a reconstruction method in Candidate.
        return Candidate.reconstruct(
            entity.getCandidateId(),
            Email.of(entity.getEmail()),
            FirstName.of(entity.getFirstName()),
            LastName.of(entity.getLastName()),
            entity.getPhoneNumber() != null ? PhoneNumber.of(entity.getPhoneNumber()) : null,
            entity.getSkills() != null ? CandidateSkills.of(entity.getSkills()) : null,
            entity.getExperienceYears() != null ? ExperienceLevel.of(entity.getExperienceYears()) : null,
            entity.getDesiredCity() != null ? DesiredLocation.of(entity.getDesiredCity(), entity.getDesiredCountry(), entity.getRemoteOk()) : null,
            entity.getStatus(),
            entity.getBio(),
            entity.getRegisteredAt(),
            entity.getProfileCompletedAt(),
            entity.getSuspendedAt(),
            entity.getDeactivatedAt()
        );
    }
}
