package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: Experience Level
 * Years of professional experience.
 * 
 * @author GitHub Copilot / TASK-014
 */
public class ExperienceLevel {
    private final int years;
    
    public static ExperienceLevel of(int years) {
        if (years < 0 || years > 70) {
            throw new IllegalArgumentException("Experience years must be between 0 and 70");
        }
        return new ExperienceLevel(years);
    }
    
    private ExperienceLevel(int years) {
        this.years = years;
    }
    
    public int getYears() {
        return years;
    }
    
    public String getLevel() {
        if (years < 1) return "ENTRY";
        if (years < 3) return "JUNIOR";
        if (years < 7) return "MID";
        if (years < 12) return "SENIOR";
        return "EXPERT";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperienceLevel)) return false;
        ExperienceLevel that = (ExperienceLevel) o;
        return years == that.years;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(years);
    }
    
    @Override
    public String toString() {
        return "ExperienceLevel{" + years + "y}";
    }
}
