package com.jrecruiter.userservice.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.jrecruiter.userservice.domain.valueobjects.CandidateProfileStatus;

@Entity
@Table(name = "candidates")
public class CandidateJpaEntity {

    @Id
    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "skills", length = 2000)
    private String skills;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "desired_city")
    private String desiredCity;

    @Column(name = "desired_country")
    private String desiredCountry;

    @Column(name = "remote_ok")
    private Boolean remoteOk;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CandidateProfileStatus status;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "profile_completed_at")
    private LocalDateTime profileCompletedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    public CandidateJpaEntity() {}

    // Getters and Setters
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
    public CandidateProfileStatus getStatus() { return status; }
    public void setStatus(CandidateProfileStatus status) { this.status = status; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getProfileCompletedAt() { return profileCompletedAt; }
    public void setProfileCompletedAt(LocalDateTime profileCompletedAt) { this.profileCompletedAt = profileCompletedAt; }
    public LocalDateTime getSuspendedAt() { return suspendedAt; }
    public void setSuspendedAt(LocalDateTime suspendedAt) { this.suspendedAt = suspendedAt; }
    public LocalDateTime getDeactivatedAt() { return deactivatedAt; }
    public void setDeactivatedAt(LocalDateTime deactivatedAt) { this.deactivatedAt = deactivatedAt; }

    // Simple Builder-like static method or manual builder if needed
    public static CandidateJpaEntityBuilder builder() {
        return new CandidateJpaEntityBuilder();
    }

    public static class CandidateJpaEntityBuilder {
        private final CandidateJpaEntity entity = new CandidateJpaEntity();
        public CandidateJpaEntityBuilder candidateId(UUID id) { entity.setCandidateId(id); return this; }
        public CandidateJpaEntityBuilder email(String email) { entity.setEmail(email); return this; }
        public CandidateJpaEntityBuilder firstName(String firstName) { entity.setFirstName(firstName); return this; }
        public CandidateJpaEntityBuilder lastName(String lastName) { entity.setLastName(lastName); return this; }
        public CandidateJpaEntityBuilder phoneNumber(String phoneNumber) { entity.setPhoneNumber(phoneNumber); return this; }
        public CandidateJpaEntityBuilder skills(String skills) { entity.setSkills(skills); return this; }
        public CandidateJpaEntityBuilder experienceYears(Integer exp) { entity.setExperienceYears(exp); return this; }
        public CandidateJpaEntityBuilder desiredCity(String city) { entity.setDesiredCity(city); return this; }
        public CandidateJpaEntityBuilder desiredCountry(String country) { entity.setDesiredCountry(country); return this; }
        public CandidateJpaEntityBuilder remoteOk(Boolean remote) { entity.setRemoteOk(remote); return this; }
        public CandidateJpaEntityBuilder status(CandidateProfileStatus status) { entity.setStatus(status); return this; }
        public CandidateJpaEntityBuilder bio(String bio) { entity.setBio(bio); return this; }
        public CandidateJpaEntityBuilder registeredAt(LocalDateTime dt) { entity.setRegisteredAt(dt); return this; }
        public CandidateJpaEntityBuilder profileCompletedAt(LocalDateTime dt) { entity.setProfileCompletedAt(dt); return this; }
        public CandidateJpaEntityBuilder suspendedAt(LocalDateTime dt) { entity.setSuspendedAt(dt); return this; }
        public CandidateJpaEntityBuilder deactivatedAt(LocalDateTime dt) { entity.setDeactivatedAt(dt); return this; }
        public CandidateJpaEntity build() { return entity; }
    }
}
