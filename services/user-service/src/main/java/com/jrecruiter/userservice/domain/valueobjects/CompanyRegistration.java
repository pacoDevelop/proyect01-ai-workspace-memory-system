package com.jrecruiter.userservice.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Company Registration Number
 * 
 * Tax ID / VAT / Company registration identifier.
 * Varies by country (not validated here, just stored).
 * 
 * @author GitHub Copilot / TASK-013
 */
public class CompanyRegistration {
    
    private final String registrationNumber;
    private final String country; // ISO 3166-1 alpha-2 code
    
    private CompanyRegistration(String registrationNumber, String country) {
        this.registrationNumber = registrationNumber;
        this.country = country;
    }
    
    /**
     * Factory method
     */
    public static CompanyRegistration of(String registrationNumber, String country) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty");
        }
        
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be empty");
        }
        
        String regTrimmed = registrationNumber.trim();
        String countryTrimmed = country.trim().toUpperCase();
        
        if (regTrimmed.length() > 50) {
            throw new IllegalArgumentException("Registration number must not exceed 50 characters");
        }
        
        if (countryTrimmed.length() != 2) {
            throw new IllegalArgumentException("Country code must be ISO 3166-1 alpha-2 (2 characters)");
        }
        
        return new CompanyRegistration(regTrimmed, countryTrimmed);
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public String getCountry() {
        return country;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyRegistration that = (CompanyRegistration) o;
        return Objects.equals(registrationNumber, that.registrationNumber) &&
               Objects.equals(country, that.country);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(registrationNumber, country);
    }
    
    @Override
    public String toString() {
        return "CompanyRegistration{" +
                "number=" + registrationNumber +
                ", country=" + country +
                '}';
    }
}
