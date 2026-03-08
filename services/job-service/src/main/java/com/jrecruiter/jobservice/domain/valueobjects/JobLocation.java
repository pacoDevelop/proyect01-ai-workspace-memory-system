package com.jrecruiter.jobservice.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Job Location
 * 
 * Represents the job location with address and geographic coordinates.
 * Immutable and self-validating.
 * 
 * @author GitHub Copilot / TASK-007
 */
public record JobLocation(
    String address1,
    String address2,
    String city,
    String state,
    String postalCode,
    String country,
    String website,
    String phone,
    String email,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean usesMap
) {
    
    public static final int MAX_ADDRESS = 100;
    public static final int MAX_CITY = 50;
    public static final int MAX_PHONE = 20;
    
    /**
     * Creates a new JobLocation with validation.
     * 
     * At least one of:
     * - address1 + city + state must be provided (physical address)
     * - latitude + longitude must be provided (coordinates)
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public JobLocation {
        Objects.requireNonNull(country, "Country cannot be null");
        
        if (country.length() > 50) {
            throw new IllegalArgumentException("Country name too long");
        }
        
        // Validate that we have either address OR coordinates
        boolean hasAddress = address1 != null && !address1.trim().isEmpty() 
                          && city != null && !city.trim().isEmpty()
                          && state != null && !state.trim().isEmpty();
        
        boolean hasCoordinates = latitude != null && longitude != null
                              && latitude.doubleValue() >= -90 && latitude.doubleValue() <= 90
                              && longitude.doubleValue() >= -180 && longitude.doubleValue() <= 180;
        
        if (!hasAddress && !hasCoordinates) {
            throw new IllegalArgumentException(
                "Job location must have either physical address (address1, city, state) or coordinates (latitude, longitude)"
            );
        }
        
        // Validate address fields if provided
        if (address1 != null && address1.length() > MAX_ADDRESS) {
            throw new IllegalArgumentException("Address1 too long");
        }
        if (city != null && city.length() > MAX_CITY) {
            throw new IllegalArgumentException("City name too long");
        }
        if (phone != null && phone.length() > MAX_PHONE) {
            throw new IllegalArgumentException("Phone number too long");
        }
    }
    
    /**
     * Creates a JobLocation with physical address only (no coordinates).
     */
    public static JobLocation ofAddress(
            String address1, String address2, String city, String state,
            String postalCode, String country, String website, String phone, String email) {
        return new JobLocation(address1, address2, city, state, postalCode, country,
            website, phone, email, null, null, false);
    }
    
    /**
     * Creates a JobLocation with coordinates only (no physical address).
     */
    public static JobLocation ofCoordinates(
            String country, BigDecimal latitude, BigDecimal longitude,
            String website, String phone, String email) {
        return new JobLocation(null, null, null, null, null, country,
            website, phone, email, latitude, longitude, true);
    }
    
    /**
     * Creates a JobLocation with both address and coordinates.
     */
    public static JobLocation ofBoth(
            String address1, String address2, String city, String state,
            String postalCode, String country, BigDecimal latitude, BigDecimal longitude,
            String website, String phone, String email) {
        return new JobLocation(address1, address2, city, state, postalCode, country,
            website, phone, email, latitude, longitude, true);
    }
}
