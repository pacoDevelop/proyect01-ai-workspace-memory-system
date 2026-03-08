package com.jrecruiter.jobservice.application.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO: Job Response
 * 
 * Output format for Job data in REST responses.
 * @JsonInclude ignores null fields for cleaner JSON.
 * 
 * @author GitHub Copilot / TASK-010
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobResponse {
    
    private UUID jobId;
    private String universalId;
    private UUID employerId;
    private UUID industryId;
    private UUID regionId;
    
    private String title;
    private String description;
    private String companyName;
    
    private LocationResponse location;
    private SalaryResponse salary;
    private String offeredBy;
    
    private String status;
    private Instant createdAt;
    private Instant publishedAt;
    private Instant closedAt;
    private Instant updatedAt;
    
    // Constructors
    
    public JobResponse() {
    }
    
    public JobResponse(
            UUID jobId,
            String universalId,
            UUID employerId,
            UUID industryId,
            UUID regionId,
            String title,
            String description,
            String companyName,
            LocationResponse location,
            SalaryResponse salary,
            String offeredBy,
            String status,
            Instant createdAt,
            Instant publishedAt,
            Instant closedAt,
            Instant updatedAt) {
        this.jobId = jobId;
        this.universalId = universalId;
        this.employerId = employerId;
        this.industryId = industryId;
        this.regionId = regionId;
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.location = location;
        this.salary = salary;
        this.offeredBy = offeredBy;
        this.status = status;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.closedAt = closedAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters & Setters
    
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    
    public String getUniversalId() { return universalId; }
    public void setUniversalId(String universalId) { this.universalId = universalId; }
    
    public UUID getEmployerId() { return employerId; }
    public void setEmployerId(UUID employerId) { this.employerId = employerId; }
    
    public UUID getIndustryId() { return industryId; }
    public void setIndustryId(UUID industryId) { this.industryId = industryId; }
    
    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public LocationResponse getLocation() { return location; }
    public void setLocation(LocationResponse location) { this.location = location; }
    
    public SalaryResponse getSalary() { return salary; }
    public void setSalary(SalaryResponse salary) { this.salary = salary; }
    
    public String getOfferedBy() { return offeredBy; }
    public void setOfferedBy(String offeredBy) { this.offeredBy = offeredBy; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // ========================================================================
    // NESTED DTOs
    // ========================================================================
    
    public static class LocationResponse {
        private String street;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String country;
        private String countryCode;
        private Double latitude;
        private Double longitude;
        private Boolean remote;
        
        public LocationResponse() {
        }
        
        public LocationResponse(String street, String city, String stateProvince, String postalCode,
                String country, String countryCode, Double latitude, Double longitude, Boolean remote) {
            this.street = street;
            this.city = city;
            this.stateProvince = stateProvince;
            this.postalCode = postalCode;
            this.country = country;
            this.countryCode = countryCode;
            this.latitude = latitude;
            this.longitude = longitude;
            this.remote = remote;
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
        public void setRemote(Boolean remote) { this.remote = remote; }
    }
    
    public static class SalaryResponse {
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private String currency;
        private String frequency;
        
        public SalaryResponse() {
        }
        
        public SalaryResponse(BigDecimal minAmount, BigDecimal maxAmount, String currency, String frequency) {
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
