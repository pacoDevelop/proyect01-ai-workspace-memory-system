package com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.CandidateJpaEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateJpaRepository extends JpaRepository<CandidateJpaEntity, UUID> {
    Optional<CandidateJpaEntity> findByEmail(String email);
    long countByStatus(String status);
}
