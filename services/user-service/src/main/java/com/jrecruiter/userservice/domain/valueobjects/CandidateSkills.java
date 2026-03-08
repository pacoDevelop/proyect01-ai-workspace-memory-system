package com.jrecruiter.userservice.domain.valueobjects;

/**
 * Value Object: Candidate Skills
 * Collection of professional skills.
 * 
 * @author GitHub Copilot / TASK-014
 */
public class CandidateSkills {
    private final String value; // Comma-separated skills
    
    private static final int MAX_LENGTH = 2000;
    private static final int MAX_SKILLS = 50;
    
    public static CandidateSkills of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("At least one skill is required");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Skills cannot exceed %d characters", MAX_LENGTH)
            );
        }
        
        String[] skills = trimmed.split(",");
        if (skills.length > MAX_SKILLS) {
            throw new IllegalArgumentException(
                String.format("Cannot have more than %d skills", MAX_SKILLS)
            );
        }
        
        // Validate each skill is non-empty
        for (String skill : skills) {
            if (skill.trim().isEmpty()) {
                throw new IllegalArgumentException("Skills cannot be empty");
            }
        }
        
        return new CandidateSkills(trimmed);
    }
    
    private CandidateSkills(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public String[] getSkillsArray() {
        return value.split(",");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateSkills)) return false;
        CandidateSkills that = (CandidateSkills) o;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return "CandidateSkills{" + value + '}';
    }
}
