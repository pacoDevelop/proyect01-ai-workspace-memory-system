package com.jrecruiter.jobservice.infrastructure.persistence;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable Type: Job Salary
 * 
 * Persists the JobSalary value object as an embeddable component
 * within the jobs table using JSON columns.
 * 
 * @author GitHub Copilot / TASK-009
 */
@Embeddable
public class JobSalaryEmbeddable implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "salary_min_amount", nullable = true)
    private BigDecimal minAmount;
    
    @Column(name = "salary_max_amount", nullable = true)
    private BigDecimal maxAmount;
    
    @Column(name = "salary_currency", length = 3, nullable = true)
    private String currency;
    
    @Column(name = "salary_frequency", length = 20, nullable = true)
    private String frequency;
    
    // Constructors
    
    public JobSalaryEmbeddable() {
        // JPA-required no-arg constructor
    }
    
    public JobSalaryEmbeddable(BigDecimal minAmount, BigDecimal maxAmount, String currency, String frequency) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.currency = currency;
        this.frequency = frequency;
    }
    
    // Getters
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public String getFrequency() {
        return frequency;
    }
}
