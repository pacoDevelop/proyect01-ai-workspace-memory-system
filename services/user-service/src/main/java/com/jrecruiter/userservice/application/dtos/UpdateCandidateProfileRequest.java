package com.jrecruiter.userservice.application.dtos;

public class UpdateCandidateProfileRequest {
    private String skills;
    private Integer experienceYears;
    private String desiredCity;
    private String desiredCountry;
    private Boolean remoteOk;
    private String bio;

    public UpdateCandidateProfileRequest() {}

    // Getters/Setters
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
}
