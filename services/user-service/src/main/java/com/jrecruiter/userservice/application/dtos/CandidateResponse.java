package com.jrecruiter.userservice.application.dtos;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO: Candidate Response
 * 
 * @author GitHub Copilot / TASK-014
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateResponse {
    
    private UUID candidateId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String status;
    private String skills;
    private Integer experienceYears;
    private String desiredCity;
    private String desiredCountry;
    private Boolean remoteOk;
    private String bio;
    private LocalDateTime registeredAt;
    private LocalDateTime profileCompletedAt;
    
    public CandidateResponse() {}
    
    // Getters/Setters
    public UUID getCandidateId() { return candidateId; }
    public void setCandidateId(UUID candidateId) { this.candidateId = candidateId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    
    public String getDesiredCity() { return desiredCity; }
    public void setDesiredCity(String desiredCity) { this.desiredCity = desiredCity; }
    
    public String getDesiredCountry() { return desiredCountry; }
    public void setDesiredCountry(String desiredCountry) { this.desiredCountry = desiredCountry; }
    
    public Boolean getRemoteOk() { return remoteOk; }
    public void setRemoteOk(Boolean remoteOk) { this.remoteOk = remoteOk; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    
    public LocalDateTime getProfileCompletedAt() { return profileCompletedAt; }
    public void setProfileCompletedAt(LocalDateTime profileCompletedAt) { this.profileCompletedAt = profileCompletedAt; }
}
