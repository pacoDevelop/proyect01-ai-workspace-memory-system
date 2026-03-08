package com.jrecruiter.jobservice.infrastructure.persistence;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable Type: Job Location
 * 
 * Persists the JobLocation value object as an embeddable component
 * within the jobs table.
 * 
 * Stores location either as address string OR as coordinates (latitude/longitude).
 * 
 * @author GitHub Copilot / TASK-009
 */
@Embeddable
public class JobLocationEmbeddable implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Address components
    @Column(name = "location_street", length = 255, nullable = true)
    private String street;
    
    @Column(name = "location_city", length = 100, nullable = true)
    private String city;
    
    @Column(name = "location_state_province", length = 100, nullable = true)
    private String stateProvince;
    
    @Column(name = "location_postal_code", length = 50, nullable = true)
    private String postalCode;
    
    @Column(name = "location_country", length = 100, nullable = true)
    private String country;
    
    @Column(name = "location_country_code", length = 2, nullable = true)
    private String countryCode;
    
    // Coordinates
    @Column(name = "location_latitude", nullable = true)
    private BigDecimal latitude;
    
    @Column(name = "location_longitude", nullable = true)
    private BigDecimal longitude;
    
    // Remote work flag
    @Column(name = "location_remote", nullable = false)
    private Boolean remote = false;
    
    // Constructors
    
    public JobLocationEmbeddable() {
        // JPA-required no-arg constructor
        this.remote = false;
    }
    
    public JobLocationEmbeddable(
            String street,
            String city,
            String stateProvince,
            String postalCode,
            String country,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            Boolean remote) {
        this.street = street;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.country = country;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.remote = remote != null ? remote : false;
    }
    
    // Getters
    
    public String getStreet() {
        return street;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getStateProvince() {
        return stateProvince;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public Boolean getRemote() {
        return remote;
    }
}
