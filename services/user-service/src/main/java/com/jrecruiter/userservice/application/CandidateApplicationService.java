package com.jrecruiter.userservice.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jrecruiter.userservice.domain.aggregates.Candidate;
import com.jrecruiter.userservice.domain.repositories.CandidateRepository;
import com.jrecruiter.userservice.domain.valueobjects.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateApplicationService {

    private final CandidateRepository candidateRepository;

    @Transactional
    public UUID registerCandidate(String email, String firstName, String lastName, String phone) {
        Candidate candidate = Candidate.registerCandidate(
            Email.of(email),
            FirstName.of(firstName),
            LastName.of(lastName),
            PhoneNumber.of(phone)
        );
        
        Candidate saved = candidateRepository.save(candidate);
        // TODO: Publish events
        return saved.getCandidateId();
    }

    @Transactional
    public void completeProfile(UUID candidateId, String skills, int expYears, String city, String country, boolean remoteOk, String bio) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
            
        candidate.completeProfile(
            CandidateSkills.of(skills),
            ExperienceLevel.of(expYears),
            DesiredLocation.of(city, country, remoteOk),
            bio
        );
        
        candidateRepository.save(candidate);
        // TODO: Publish events
    }

    @Transactional(readOnly = true)
    public Candidate getCandidate(UUID candidateId) {
        return candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
    }
}
