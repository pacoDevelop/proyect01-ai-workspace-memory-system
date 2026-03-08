package com.jrecruiter.userservice.infrastructure.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.jrecruiter.userservice.application.CandidateApplicationService;
import com.jrecruiter.userservice.application.dtos.*;
import com.jrecruiter.userservice.domain.aggregates.Candidate;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateApplicationService candidateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID registerCandidate(@RequestBody @Valid RegisterCandidateRequest request) {
        return candidateService.registerCandidate(
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPhoneNumber()
        );
    }

    @PutMapping("/{candidateId}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeProfile(
            @PathVariable UUID candidateId,
            @RequestBody @Valid UpdateCandidateProfileRequest request) {
        candidateService.completeProfile(
            candidateId,
            request.getSkills(),
            request.getExperienceYears(),
            request.getDesiredCity(),
            request.getDesiredCountry(),
            request.getRemoteOk(),
            request.getBio()
        );
    }

    @GetMapping("/{candidateId}")
    public CandidateResponse getCandidate(@PathVariable UUID candidateId) {
        Candidate candidate = candidateService.getCandidate(candidateId);
        return mapToResponse(candidate);
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        CandidateResponse response = new CandidateResponse();
        response.setCandidateId(candidate.getCandidateId());
        response.setEmail(candidate.getEmail().getValue());
        response.setFirstName(candidate.getFirstName().getValue());
        response.setLastName(candidate.getLastName().getValue());
        response.setPhoneNumber(candidate.getPhoneNumber() != null ? candidate.getPhoneNumber().getValue() : null);
        response.setStatus(candidate.getStatus().name());
        response.setSkills(candidate.getSkills() != null ? candidate.getSkills().getValue() : null);
        response.setExperienceYears(candidate.getExperienceLevel() != null ? candidate.getExperienceLevel().getYears() : null);
        response.setDesiredLocation(candidate.getDesiredLocation() != null ? 
            String.format("%s, %s (Remote: %b)", 
                candidate.getDesiredLocation().getCity(), 
                candidate.getDesiredLocation().getCountry(), 
                candidate.getDesiredLocation().isRemoteOk()) : null);
        response.setBio(candidate.getBio());
        response.setRegisteredAt(candidate.getRegisteredAt());
        response.setProfileCompletedAt(candidate.getProfileCompletedAt());
        return response;
    }
}
