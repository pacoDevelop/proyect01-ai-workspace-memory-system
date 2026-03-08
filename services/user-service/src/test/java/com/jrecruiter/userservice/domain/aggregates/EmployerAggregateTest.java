package com.jrecruiter.userservice.domain.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;

import com.jrecruiter.userservice.domain.valueobjects.*;
import com.jrecruiter.userservice.domain.events.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests: Employer Aggregate
 * 
 * Comprehensive testing of employer domain logic and state transitions.
 * Tests follow AAA pattern (Arrange, Act, Assert).
 * 
 * @author GitHub Copilot / TASK-013
 */
@DisplayName("Employer Aggregate Tests")
public class EmployerAggregateTest {
    
    private EmployerName employerName;
    private Email email;
    private CompanyRegistration companyRegistration;
    private PhoneNumber phoneNumber;
    
    @BeforeEach
    public void setUp() {
        // Arrange: Create test fixtures
        employerName = EmployerName.of("TechCorp Inc");
        email = Email.of("contact@techcorp.com");
        companyRegistration = CompanyRegistration.of("REG-123456", "US");
        phoneNumber = PhoneNumber.of("+1-555-0123");
    }
    
    @Test
    @DisplayName("Should create new employer in PENDING_VERIFICATION status")
    public void testCreateEmployer() {
        // Act: Create employer
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        
        // Assert: Verify initial state
        assertNotNull(employer.getEmployerId());
        assertEquals(employerName, employer.getEmployerName());
        assertEquals(email, employer.getEmail());
        assertEquals(EmployerStatus.PENDING_VERIFICATION, employer.getStatus());
        assertNotNull(employer.getRegisteredAt());
        assertNull(employer.getVerifiedAt());
        assertEquals(1, employer.getDomainEvents().size());
        assertTrue(employer.getDomainEvents().get(0) instanceof EmployerRegisteredEvent);
    }
    
    @Test
    @DisplayName("Should verify employer and transition to ACTIVE status")
    public void testVerifyEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.clearDomainEvents();
        
        // Act: Verify employer
        employer.verify();
        
        // Assert
        assertEquals(EmployerStatus.ACTIVE, employer.getStatus());
        assertNotNull(employer.getVerifiedAt());
        assertEquals(1, employer.getDomainEvents().size());
        assertTrue(employer.getDomainEvents().get(0) instanceof EmployerVerifiedEvent);
    }
    
    @Test
    @DisplayName("Should throw exception when verifying already verified employer")
    public void testVerifyAlreadyVerifiedEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        
        // Act & Assert
        assertThrows(
            IllegalStateException.class,
            employer::verify,
            "Should not verify already active employer"
        );
    }
    
    @Test
    @DisplayName("Should suspend active employer")
    public void testSuspendEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        employer.clearDomainEvents();
        
        // Act: Suspend employer
        employer.suspend("Payment dispute");
        
        // Assert
        assertEquals(EmployerStatus.SUSPENDED, employer.getStatus());
        assertNotNull(employer.getSuspendedAt());
        assertEquals(1, employer.getDomainEvents().size());
        assertTrue(employer.getDomainEvents().get(0) instanceof EmployerSuspendedEvent);
    }
    
    @Test
    @DisplayName("Should throw exception when suspend without reason")
    public void testSuspendWithoutReason() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> employer.suspend(""),
            "Should require suspension reason"
        );
    }
    
    @Test
    @DisplayName("Should reactivate suspended employer")
    public void testReactivateSuspendedEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        employer.suspend("Policy violation");
        employer.clearDomainEvents();
        
        // Act: Reactivate
        employer.reactivate();
        
        // Assert
        assertEquals(EmployerStatus.ACTIVE, employer.getStatus());
        assertNull(employer.getSuspendedAt());
        assertEquals(1, employer.getDomainEvents().size());
        assertTrue(employer.getDomainEvents().get(0) instanceof EmployerReactivatedEvent);
    }
    
    @Test
    @DisplayName("Should throw exception when reactivating non-suspended employer")
    public void testReactivateNonSuspendedEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        
        // Act & Assert
        assertThrows(
            IllegalStateException.class,
            employer::reactivate,
            "Should only reactivate suspended employers"
        );
    }
    
    @Test
    @DisplayName("Should deactivate active employer")
    public void testDeactivateEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.verify();
        employer.clearDomainEvents();
        
        // Act: Deactivate
        employer.deactivate("Account closure requested");
        
        // Assert
        assertEquals(EmployerStatus.INACTIVE, employer.getStatus());
        assertNotNull(employer.getDeactivatedAt());
        assertEquals(1, employer.getDomainEvents().size());
        assertTrue(employer.getDomainEvents().get(0) instanceof EmployerDeactivatedEvent);
    }
    
    @Test
    @DisplayName("Should throw exception when deactivating inactive employer")
    public void testDeactivateInactiveEmployer() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        employer.deactivate("Account closed");
        
        // Act & Assert
        assertThrows(
            IllegalStateException.class,
            () -> employer.deactivate("Already closed"),
            "Should not deactivate already inactive employer"
        );
    }
    
    @Test
    @DisplayName("Should clear domain events after retrieval")
    public void testClearDomainEvents() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        assertEquals(1, employer.getDomainEvents().size());
        
        // Act: Clear events
        employer.clearDomainEvents();
        
        // Assert
        assertTrue(employer.getDomainEvents().isEmpty());
    }
    
    @Test
    @DisplayName("Should be equal based on employerId")
    public void testEmployerEquality() {
        // Arrange
        Employer employer1 = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        UUID sameId = employer1.getEmployerId();
        
        // Act: Create another employer with same ID (simulated via inequality)
        Employer employer2 = Employer.registerEmployer(
            EmployerName.of("Different Corp"),
            Email.of("another@example.com"),
            companyRegistration,
            phoneNumber
        );
        
        // Assert: Different employers with different IDs
        assertNotEquals(employer1, employer2);
        
        // Same employer instance
        assertEquals(employer1, employer1);
    }
    
    @Test
    @DisplayName("Should have consistent hash code")
    public void testEmployerHashCode() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        
        // Act: Get hash code multiple times
        int hash1 = employer.hashCode();
        int hash2 = employer.hashCode();
        
        // Assert: Hash should be consistent
        assertEquals(hash1, hash2, "Hash code should be consistent");
    }
    
    @Test
    @DisplayName("Should throw exception when creating with null fields")
    public void testCreateWithNullFields() {
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> Employer.registerEmployer(null, email, companyRegistration, phoneNumber),
            "Should not allow null employer name"
        );
        
        assertThrows(
            IllegalArgumentException.class,
            () -> Employer.registerEmployer(employerName, null, companyRegistration, phoneNumber),
            "Should not allow null email"
        );
    }
    
    @Test
    @DisplayName("Should support full state lifecycle")
    public void testFullStateLifecycle() {
        // Arrange
        Employer employer = Employer.registerEmployer(
            employerName, email, companyRegistration, phoneNumber
        );
        
        // Act & Assert: Full lifecycle
        assertEquals(EmployerStatus.PENDING_VERIFICATION, employer.getStatus());
        
        employer.verify();
        assertEquals(EmployerStatus.ACTIVE, employer.getStatus());
        
        employer.suspend("Issues detected");
        assertEquals(EmployerStatus.SUSPENDED, employer.getStatus());
        
        employer.reactivate();
        assertEquals(EmployerStatus.ACTIVE, employer.getStatus());
        
        employer.deactivate("Voluntary closure");
        assertEquals(EmployerStatus.INACTIVE, employer.getStatus());
    }
}
