package com.jrecruiter.userservice.application.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SubmitApplicationRequest {
    @NotNull
    private UUID candidateId;
    
    @NotNull
    private UUID jobId;
    
    private String coverLetter;

    public SubmitApplicationRequest() {}

    // Getters/Setters
    public UUID getCandidateId() { return candidateId; }
    public void setCandidateId(UUID candidateId) { this.candidateId = candidateId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
