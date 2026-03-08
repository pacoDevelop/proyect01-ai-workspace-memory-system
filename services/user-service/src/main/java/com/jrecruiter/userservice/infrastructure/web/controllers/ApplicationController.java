package com.jrecruiter.userservice.infrastructure.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.jrecruiter.userservice.application.JobApplicationService;
import com.jrecruiter.userservice.application.dtos.*;
import com.jrecruiter.userservice.domain.aggregates.Application;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID submitApplication(@RequestBody @Valid SubmitApplicationRequest request) {
        return applicationService.submitApplication(
            request.getCandidateId(),
            request.getJobId(),
            request.getCoverLetter()
        );
    }

    @PatchMapping("/{applicationId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(
            @PathVariable UUID applicationId,
            @RequestParam Application.ApplicationStatus status,
            @RequestParam(required = false) String reason) {
        applicationService.updateApplicationStatus(applicationId, status, reason);
    }

    @GetMapping("/candidate/{candidateId}")
    public List<ApplicationResponse> getCandidateApplications(@PathVariable UUID candidateId) {
        return applicationService.getCandidateApplications(candidateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/job/{jobId}")
    public List<ApplicationResponse> getJobApplications(@PathVariable UUID jobId) {
        return applicationService.getJobApplications(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApplicationResponse mapToResponse(Application app) {
        ApplicationResponse response = new ApplicationResponse();
        response.setApplicationId(app.getApplicationId());
        response.setCandidateId(app.getCandidateId());
        response.setJobId(app.getJobId());
        response.setStatus(app.getStatus());
        response.setCoverLetter(app.getCoverLetter());
        response.setSubmittedAt(app.getSubmittedAt());
        response.setStatusChangedAt(app.getStatusChangedAt());
        response.setRejectionReason(app.getRejectionReason());
        return response;
    }
}
