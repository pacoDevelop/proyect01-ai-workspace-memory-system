package com.jrecruiter.jobservice.application.dtos;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO: Update Job Request
 * 
 * Input validation for PUT /api/jobs/:id endpoint.
 * Only allows updating mutable fields.
 * 
 * @author GitHub Copilot / TASK-010
 */
public class UpdateJobRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 10000, message = "Description must be between 20 and 10000 characters")
    private String description;
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;
    
    @Valid
    private CreateJobRequest.LocationRequest location;
    
    @Valid
    private CreateJobRequest.SalaryRequest salary;
    
    // Constructors
    
    public UpdateJobRequest() {
    }
    
    public UpdateJobRequest(
            String title,
            String description,
            String companyName,
            CreateJobRequest.LocationRequest location,
            CreateJobRequest.SalaryRequest salary) {
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
    }
    
    // Getters & Setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public CreateJobRequest.LocationRequest getLocation() {
        return location;
    }
    
    public void setLocation(CreateJobRequest.LocationRequest location) {
        this.location = location;
    }
    
    public CreateJobRequest.SalaryRequest getSalary() {
        return salary;
    }
    
    public void setSalary(CreateJobRequest.SalaryRequest salary) {
        this.salary = salary;
    }
}
