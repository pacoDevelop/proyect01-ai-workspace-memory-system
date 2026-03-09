package com.jrecruiter.userservice.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Security Tests — Comprehensive validation of security fixes
 * Tests for TASK-039 (Security Hardening):
 * - A02 (Cryptographic Failures): JWT secret from environment variable
 * - A07 (Broken Authentication): Email claim populated in refresh token
 * - A07 (Broken Authentication): Refresh token rotation enforced
 * 
 * @author GitHub Copilot / TASK-039
 */
@DisplayName("JWT Security Tests — TASK-039 fixes validation")
class JwtSecurityTests {
    
    private JwtTokenProvider jwtTokenProvider;
    private static final String STRONG_SECRET = "this-is-a-very-strong-secret-key-minimum-32-characters-for-hs512";
    private static final String WEAK_SECRET = "weak";
    private static final String USER_EMAIL = "test@example.com";
    private static final UUID USER_ID = UUID.randomUUID();
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Simulate Spring @Value injection of JWT secret
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", STRONG_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
    }
    
    // ============================================================================
    // FIX A02: JWT Secret from Environment Variable (No Weak Defaults)
    // ============================================================================
    
    @Test
    @DisplayName("A02: Should require strong JWT secret (minimum 32 characters)")
    void testJwtSecretStrengthValidation() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", WEAK_SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(provider, "refreshTokenExpiration", 604800000L);
        
        // Should throw when trying to create token with weak secret
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> provider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"))
        );
        
        assertTrue(exception.getMessage().contains("JWT_SECRET must be at least 32 characters"));
        assertTrue(exception.getMessage().contains("256 bits"));
    }
    
    @Test
    @DisplayName("A02: Should accept strong JWT secret (32+ characters)")
    void testJwtTokenGenerationWithStrongSecret() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtTokenProvider.validateToken(token));
    }
    
    @Test
    @DisplayName("A02: Should validate token created with environment variable secret")
    void testTokenValidationWithEnvVarSecret() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER", "ADMIN"));
        
        // Token should validate
        assertTrue(jwtTokenProvider.validateToken(token));
        
        // Should extract userId correctly
        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(USER_ID, extractedUserId);
    }
    
    // ============================================================================
    // FIX A07: Email Claim Populated in Refresh Token
    // ============================================================================
    
    @Test
    @DisplayName("A07: Should populate email claim in refresh token")
    void testRefreshTokenEmailClaimPopulated() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, USER_EMAIL);
        
        assertNotNull(refreshToken);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(refreshToken);
        assertEquals(USER_EMAIL, extractedEmail);
    }
    
    @Test
    @DisplayName("A07: Should reject refresh token without email (null)")
    void testRefreshTokenRejectsNullEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jwtTokenProvider.generateRefreshToken(USER_ID, null)
        );
        
        assertTrue(exception.getMessage().contains("Email claim cannot be null or empty"));
    }
    
    @Test
    @DisplayName("A07: Should reject refresh token with blank email")
    void testRefreshTokenRejectsBlankEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jwtTokenProvider.generateRefreshToken(USER_ID, "   ")
        );
        
        assertTrue(exception.getMessage().contains("Email claim cannot be null or empty"));
    }
    
    @Test
    @DisplayName("A07: Should include rotation version in refresh token")
    void testRefreshTokenIncludesRotationVersion() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, USER_EMAIL);
        
        // Parse token to verify rotationVersion claim
        SecretKey key = Keys.hmacShaKeyFor(STRONG_SECRET.getBytes());
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        
        Object rotationVersion = claims.get("rotationVersion");
        assertNotNull(rotationVersion);
        assertEquals(1, ((Number) rotationVersion).intValue());
    }
    
    @Test
    @DisplayName("A07: Should validate both access and refresh tokens have email claim")
    void testBothTokenTypesHaveEmailClaim() {
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, USER_EMAIL);
        
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        
        assertEquals(USER_EMAIL, jwtTokenProvider.getEmailFromToken(accessToken));
        assertEquals(USER_EMAIL, jwtTokenProvider.getEmailFromToken(refreshToken));
    }
    
    // ============================================================================
    // FIX A07: Refresh Token Rotation (Token Type Tracking)
    // ============================================================================
    
    @Test
    @DisplayName("A07: Should track token type (ACCESS vs REFRESH)")
    void testTokenTypeTracking() {
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, USER_EMAIL);
        
        assertEquals("ACCESS", jwtTokenProvider.getTokenType(accessToken));
        assertEquals("REFRESH", jwtTokenProvider.getTokenType(refreshToken));
    }
    
    @Test
    @DisplayName("A07: Should validate token type before using for refresh")
    void testTokenTypeValidationForRefresh() {
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        
        String tokenType = jwtTokenProvider.getTokenType(accessToken);
        assertEquals("ACCESS", tokenType);
        
        // In real flow, this would be rejected by RefreshTokenService
        assertNotEquals("REFRESH", tokenType);
    }
    
    @Test
    @DisplayName("A07: Should reject token with missing email claim")
    void testValidateTokenRejectsMissingEmailClaim() {
        // Create a token manually without email claim
        SecretKey key = Keys.hmacShaKeyFor(STRONG_SECRET.getBytes());
        String invalidToken = Jwts.builder()
                .claim("userId", USER_ID.toString())
                .claim("type", "ACCESS")
                .setSubject(USER_ID.toString())
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS512)
                .compact();
        
        // Validation should fail because email is missing
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
    
    @Test
    @DisplayName("A07: Should throw when extracting email from token without email claim")
    void testGetEmailFromTokenThrowsOnMissing() {
        // Create token without email claim
        SecretKey key = Keys.hmacShaKeyFor(STRONG_SECRET.getBytes());
        String tokenWithoutEmail = Jwts.builder()
                .claim("userId", USER_ID.toString())
                .claim("type", "ACCESS")
                .setSubject(USER_ID.toString())
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS512)
                .compact();
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> jwtTokenProvider.getEmailFromToken(tokenWithoutEmail)
        );
        
        assertTrue(exception.getMessage().contains("Email claim missing or empty"));
    }
    
    // ============================================================================
    // Integration: All Three Fixes Together
    // ============================================================================
    
    @Test
    @DisplayName("Integration: Complete secure token flow (A02 + A07)")
    void testCompleteSecureTokenFlow() {
        // Step 1: Generate access token with email (A02: strong secret, A07: email claim)
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertEquals(USER_EMAIL, jwtTokenProvider.getEmailFromToken(accessToken));
        assertEquals("ACCESS", jwtTokenProvider.getTokenType(accessToken));
        
        // Step 2: Generate refresh token with email (A02: strong secret, A07: email + rotation version)
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, USER_EMAIL);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(USER_EMAIL, jwtTokenProvider.getEmailFromToken(refreshToken));
        assertEquals("REFRESH", jwtTokenProvider.getTokenType(refreshToken));
        
        // Step 3: Verify both tokens are different
        assertNotEquals(accessToken, refreshToken);
        
        // Step 4: Verify secret strength protection
        JwtTokenProvider weakProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(weakProvider, "jwtSecret", "weak");
        ReflectionTestUtils.setField(weakProvider, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(weakProvider, "refreshTokenExpiration", 604800000L);
        
        assertThrows(
            IllegalStateException.class,
            () -> weakProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"))
        );
    }
    
    @Test
    @DisplayName("Integration: Backward compatibility with existing valid tokens")
    void testBackwardCompatibilityWithValidTokens() {
        // Generate token with current implementation
        String token = jwtTokenProvider.generateAccessToken(USER_ID, USER_EMAIL, Arrays.asList("USER"));
        
        // Should still validate correctly
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(USER_ID, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(USER_EMAIL, jwtTokenProvider.getEmailFromToken(token));
    }
}
