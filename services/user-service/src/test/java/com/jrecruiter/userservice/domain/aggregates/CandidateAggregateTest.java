package com.jrecruiter.userservice.domain.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.jrecruiter.userservice.domain.valueobjects.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests: Candidate Aggregate
 * 
 * @author GitHub Copilot / TASK-014
 */
@DisplayName("Candidate Aggregate Tests")
public class CandidateAggregateTest {
    
    private Email email;
    private FirstName firstName;
    private LastName lastName;
    private PhoneNumber phoneNumber;
    private CandidateSkills skills;
    private ExperienceLevel experience;
    private DesiredLocation location;
    
    @BeforeEach
    public void setUp() {
        email = Email.of("candidate@example.com");
        firstName = FirstName.of("John");
        lastName = LastName.of("Doe");
        phoneNumber = PhoneNumber.of("+1-555-0123");
        skills = CandidateSkills.of("Java, Spring, PostgreSQL");
        experience = ExperienceLevel.of(5);
        location = DesiredLocation.of("New York", "USA", true);
    }
    
    @Test
    @DisplayName("Should create new candidate in PENDING_COMPLETION status")
    public void testCreateCandidate() {
        Candidate candidate = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        
        assertNotNull(candidate.getCandidateId());
        assertEquals(email, candidate.getEmail());
        assertEquals(firstName, candidate.getFirstName());
        assertEquals(CandidateProfileStatus.PENDING_COMPLETION, candidate.getStatus());
        assertNotNull(candidate.getRegisteredAt());
        assertEquals(1, candidate.getDomainEvents().size());
    }
    
    @Test
    @DisplayName("Should complete candidate profile")
    public void testCompleteProfile() {
        Candidate candidate = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        candidate.clearDomainEvents();
        
        candidate.completeProfile(skills, experience, location, "Senior Java Developer");
        
        assertEquals(CandidateProfileStatus.ACTIVE, candidate.getStatus());
        assertNotNull(candidate.getProfileCompletedAt());
    }
    
    @Test
    @DisplayName("Should suspend active candidate")
    public void testSuspendCandidate() {
        Candidate candidate = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        candidate.completeProfile(skills, experience, location, "bio");
        candidate.clearDomainEvents();
        
        candidate.suspend("Policy violation");
        
        assertEquals(CandidateProfileStatus.SUSPENDED, candidate.getStatus());
        assertNotNull(candidate.getSuspendedAt());
    }
    
    @Test
    @DisplayName("Should reactivate suspended candidate")
    public void testReactiveCandidate() {
        Candidate candidate = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        candidate.completeProfile(skills, experience, location, "bio");
        candidate.suspend("Issue");
        candidate.clearDomainEvents();
        
        candidate.reactivate();
        
        assertEquals(CandidateProfileStatus.ACTIVE, candidate.getStatus());
        assertNull(candidate.getSuspendedAt());
    }
    
    @Test
    @DisplayName("Should deactivate candidate")
    public void testDeactivateCandidate() {
        Candidate candidate = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        candidate.completeProfile(skills, experience, location, "bio");
        candidate.clearDomainEvents();
        
        candidate.deactivate("Account closure");
        
        assertEquals(CandidateProfileStatus.INACTIVE, candidate.getStatus());
        assertNotNull(candidate.getDeactivatedAt());
    }
    
    @Test
    @DisplayName("Should throw exception with null fields")
    public void testCreateWithNullFields() {
        assertThrows(IllegalArgumentException.class, 
            () -> Candidate.registerCandidate(null, firstName, lastName, phoneNumber));
    }
    
    @Test
    @DisplayName("Should have candidate equality based on ID")
    public void testCandidateEquality() {
        Candidate candidate1 = Candidate.registerCandidate(email, firstName, lastName, phoneNumber);
        Candidate candidate2 = Candidate.registerCandidate(
            Email.of("other@example.com"), firstName, lastName, phoneNumber);
        
        assertNotEquals(candidate1, candidate2);
        assertEquals(candidate1, candidate1);
    }
}
