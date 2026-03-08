package com.jrecruiter.userservice.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

/**
 * DTO: Register Candidate Request
 * 
 * @author GitHub Copilot / TASK-014
 */
public class RegisterCandidateRequest {
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{6,20}$")
    private String phoneNumber;
    
    public RegisterCandidateRequest() {}
    
    public RegisterCandidateRequest(String email, String firstName, String lastName, String phoneNumber) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters/Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
