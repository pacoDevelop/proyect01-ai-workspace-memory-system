package com.jrecruiter.userservice.domain.repositories;

import java.util.Optional;
import java.util.UUID;
import com.jrecruiter.userservice.domain.aggregates.Candidate;

/**
 * Repository Port: CandidateRepository
 * 
 * @author GitHub Copilot / TASK-014
 */
public interface CandidateRepository {
    Candidate save(Candidate candidate);
    Optional<Candidate> findById(UUID candidateId);
    Optional<Candidate> findByEmail(String email);
    boolean existsById(UUID candidateId);
    void deleteById(UUID candidateId);
    long count();
    long countByStatus(String status);
}
