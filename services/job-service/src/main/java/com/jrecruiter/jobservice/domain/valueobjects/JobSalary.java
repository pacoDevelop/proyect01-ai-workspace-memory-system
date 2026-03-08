package com.jrecruiter.jobservice.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Job Salary
 * 
 * Represents the job salary information with currency and range.
 * Immutable and self-validating.
 * 
 * @author GitHub Copilot / TASK-007
 */
public record JobSalary(
    BigDecimal minSalary,
    BigDecimal maxSalary,
    String currency,
    SalaryFrequency frequency
) {
    
    /**
     * Salary frequency enum.
     */
    public enum SalaryFrequency {
        ANNUAL, MONTHLY, HOURLY, DAILY, PROJECT
    }
    
    public static final String DEFAULT_CURRENCY = "USD";
    
    /**
     * Creates a new JobSalary with validation.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public JobSalary {
        // Currency is required
        Objects.requireNonNull(currency, "Currency cannot be null");
        
        if (currency.length() != 3) {
            throw new IllegalArgumentException("Currency code must be 3-letter ISO code (e.g., USD, EUR)");
        }
        
        // Frequency is required
        Objects.requireNonNull(frequency, "Salary frequency cannot be null");
        
        // If both min and max are provided, validate range
        if (minSalary != null && maxSalary != null) {
            if (minSalary.signum() < 0 || maxSalary.signum() < 0) {
                throw new IllegalArgumentException("Salary cannot be negative");
            }
            if (minSalary.compareTo(maxSalary) > 0) {
                throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary");
            }
        }
        
        // At least one salary bound should be provided
        if (minSalary == null && maxSalary == null) {
            throw new IllegalArgumentException("At least one of minSalary or maxSalary must be provided");
        }
    }
    
    /**
     * Creates a JobSalary with minimum salary only.
     */
    public static JobSalary ofMinimum(BigDecimal minSalary, String currency, SalaryFrequency frequency) {
        return new JobSalary(minSalary, null, currency, frequency);
    }
    
    /**
     * Creates a JobSalary with maximum salary only.
     */
    public static JobSalary ofMaximum(BigDecimal maxSalary, String currency, SalaryFrequency frequency) {
        return new JobSalary(null, maxSalary, currency, frequency);
    }
    
    /**
     * Creates a JobSalary with salary range.
     */
    public static JobSalary ofRange(BigDecimal minSalary, BigDecimal maxSalary, String currency, SalaryFrequency frequency) {
        return new JobSalary(minSalary, maxSalary, currency, frequency);
    }
    
    /**
     * Creates a JobSalary with USD currency.
     */
    public static JobSalary inUSD(BigDecimal minSalary, BigDecimal maxSalary, SalaryFrequency frequency) {
        return new JobSalary(minSalary, maxSalary, DEFAULT_CURRENCY, frequency);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (minSalary != null) {
            sb.append(minSalary);
        }
        if (maxSalary != null) {
            if (minSalary != null) {
                sb.append(" - ");
            }
            sb.append(maxSalary);
        }
        sb.append(" ").append(currency);
        sb.append(" (").append(frequency).append(")");
        return sb.toString();
    }
}
