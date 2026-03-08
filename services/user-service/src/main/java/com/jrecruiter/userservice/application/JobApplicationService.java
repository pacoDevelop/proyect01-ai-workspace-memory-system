package com.jrecruiter.userservice.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jrecruiter.userservice.domain.aggregates.Application;
import com.jrecruiter.userservice.domain.repositories.ApplicationRepository;
import com.jrecruiter.userservice.domain.repositories.CandidateRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Service for managing job applications.
 */
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;

    @Transactional
    public UUID submitApplication(UUID candidateId, UUID jobId, String coverLetter) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new IllegalArgumentException("Candidate not found");
        }
        
        // Use factory method from Application aggregate
        Application app = Application.submit(UUID.randomUUID(), candidateId, jobId, coverLetter);
        
        Application saved = applicationRepository.save(app);
        return saved.getApplicationId();
    }

    @Transactional
    public void updateApplicationStatus(UUID applicationId, Application.ApplicationStatus newStatus, String reason) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));
            
        switch (newStatus) {
            case UNDER_REVIEW -> app.review();
            case INTERVIEW -> app.scheduleInterview();
            case REJECTED -> app.reject(reason);
            case ACCEPTED -> app.accept();
            case WITHDRAWN -> app.withdraw();
            default -> throw new IllegalArgumentException("Invalid status transition: " + newStatus);
        }
        
        applicationRepository.save(app);
    }

    @Transactional(readOnly = true)
    public List<Application> getCandidateApplications(UUID candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<Application> getJobApplications(UUID jobId) {
        return applicationRepository.findByJobId(jobId);
    }
}
