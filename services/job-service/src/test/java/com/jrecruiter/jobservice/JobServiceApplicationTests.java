package com.jrecruiter.jobservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Tests for Job Service Application
 * 
 * Uses test profile to enable H2 in-memory database instead of PostgreSQL.
 * Prevents connection failures when PostgreSQL dev instance is not running.
 */
@SpringBootTest
@ActiveProfiles("test")
class JobServiceApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
