package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: Desired Location
 * Candidate's preferred work location.
 * 
 * @author GitHub Copilot / TASK-014
 */
public class DesiredLocation {
    private final String city;
    private final String country;
    private final boolean remoteOk;
    
    public static DesiredLocation of(String city, String country, boolean remoteOk) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
        
        return new DesiredLocation(city.trim(), country.trim(), remoteOk);
    }
    
    private DesiredLocation(String city, String country, boolean remoteOk) {
        this.city = city;
        this.country = country;
        this.remoteOk = remoteOk;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getCountry() {
        return country;
    }
    
    public boolean isRemoteOk() {
        return remoteOk;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DesiredLocation)) return false;
        DesiredLocation that = (DesiredLocation) o;
        return remoteOk == that.remoteOk &&
               city.equals(that.city) &&
               country.equals(that.country);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(city, country, remoteOk);
    }
    
    @Override
    public String toString() {
        return "DesiredLocation{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", remoteOk=" + remoteOk +
                '}';
    }
}
