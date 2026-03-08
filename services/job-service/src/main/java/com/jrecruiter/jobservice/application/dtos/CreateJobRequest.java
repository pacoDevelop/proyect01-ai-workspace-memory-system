package com.jrecruiter.jobservice.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO: Create Job Request
 * 
 * Input validation for POST /api/jobs endpoint.
 * Uses Bean Validation (Jakarta Validation).
 * 
 * @author GitHub Copilot / TASK-010
 */
public class CreateJobRequest {
    
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
    @NotNull(message = "Location is required")
    private LocationRequest location;
    
    @Valid
    @NotNull(message = "Salary is required")
    private SalaryRequest salary;
    
    @NotNull(message = "Offered by is required")
    private String offeredBy; // EMPLOYER or RECRUITER
    
    @NotNull(message = "Industry ID is required")
    private UUID industryId;
    
    @NotNull(message = "Region ID is required")
    private UUID regionId;
    
    // Constructors
    
    public CreateJobRequest() {
    }
    
    public CreateJobRequest(
            String title,
            String description,
            String companyName,
            LocationRequest location,
            SalaryRequest salary,
            String offeredBy,
            UUID industryId,
            UUID regionId) {
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
        this.offeredBy = offeredBy;
        this.industryId = industryId;
        this.regionId = regionId;
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
    
    public LocationRequest getLocation() {
        return location;
    }
    
    public void setLocation(LocationRequest location) {
        this.location = location;
    }
    
    public SalaryRequest getSalary() {
        return salary;
    }
    
    public void setSalary(SalaryRequest salary) {
        this.salary = salary;
    }
    
    public String getOfferedBy() {
        return offeredBy;
    }
    
    public void setOfferedBy(String offeredBy) {
        this.offeredBy = offeredBy;
    }
    
    public UUID getIndustryId() {
        return industryId;
    }
    
    public void setIndustryId(UUID industryId) {
        this.industryId = industryId;
    }
    
    public UUID getRegionId() {
        return regionId;
    }
    
    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
    }
    
    // ========================================================================
    // NESTED DTOs
    // ========================================================================
    
    public static class LocationRequest {
        
        @Size(max = 255)
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(min = 2, max = 100)
        private String city;
        
        @Size(max = 100)
        private String stateProvince;
        
        @Size(max = 50)
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 100)
        private String country;
        
        @Size(min = 2, max = 2)
        private String countryCode; // ISO 3166-1 alpha-2
        
        @Min(value = -90, message = "Latitude must be between -90 and 90")
        private Double latitude;
        
        @Min(value = -180, message = "Longitude must be between -180 and 180")
        private Double longitude;
        
        private Boolean remote = false;
        
        // Constructors, getters, setters
        public LocationRequest() {
            this.remote = false;
        }
        
        public LocationRequest(String city, String country, String countryCode, Boolean remote) {
            this.city = city;
            this.country = country;
            this.countryCode = countryCode;
            this.remote = remote != null ? remote : false;
        }
        
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStateProvince() { return stateProvince; }
        public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Boolean getRemote() { return remote; }
        public void setRemote(Boolean remote) { this.remote = remote != null ? remote : false; }
    }
    
    public static class SalaryRequest {
        
        @NotNull(message = "Min amount is required")
        @Min(value = 0, message = "Min amount must be >= 0")
        private BigDecimal minAmount;
        
        @NotNull(message = "Max amount is required")
        @Min(value = 0, message = "Max amount must be >= 0")
        private BigDecimal maxAmount;
        
        @NotBlank(message = "Currency is required (e.g., USD, EUR)")
        @Size(min = 3, max = 3)
        private String currency;
        
        @NotBlank(message = "Frequency is required (ANNUAL, MONTHLY, HOURLY)")
        private String frequency;
        
        // Constructors, getters, setters
        public SalaryRequest() {
        }
        
        public SalaryRequest(BigDecimal minAmount, BigDecimal maxAmount, String currency, String frequency) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.currency = currency;
            this.frequency = frequency;
        }
        
        public BigDecimal getMinAmount() { return minAmount; }
        public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
        public BigDecimal getMaxAmount() { return maxAmount; }
        public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
    }
}
