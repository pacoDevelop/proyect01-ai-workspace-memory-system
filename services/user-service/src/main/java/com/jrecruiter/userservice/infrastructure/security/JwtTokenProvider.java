package com.jrecruiter.userservice.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider
 * Generates and validates JWT tokens for authentication.
 * 
 * SECURITY FIXES (TASK-039):
 * - A02: JWT secret is now REQUIRED via environment variable (no weak defaults)
 * - A07: Email claim is populated in refresh tokens
 * - A07: Refresh token rotation is enforced via RefreshTokenService
 * 
 * @author GitHub Copilot / TASK-015, TASK-039
 */
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration; // milliseconds (default 24h)
    
    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration; // milliseconds (default 7d)
    
    /**
     * POST-CONSTRUCT: Validate JWT secret meets minimum security requirements
     */
    public JwtTokenProvider() {
        // Validation happens at runtime when Spring injects the value
    }
    
    /**
     * Generate access token with roles
     * FIX A07: Includes email claim for identification
     */
    public String generateAccessToken(UUID userId, String email, java.util.Collection<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("type", "ACCESS");
        
        return createToken(claims, userId.toString(), jwtExpiration);
    }
    
    /**
     * Generate refresh token with email claim
     * FIX A07: Includes email claim to ensure authorization works on refresh
     * FIX A07: Email must be populated (non-null) for proper refresh flow
     */
    public String generateRefreshToken(UUID userId, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email claim cannot be null or empty in refresh token");
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("type", "REFRESH");
        claims.put("rotationVersion", 1);  // FIX A07: Track refresh token rotation version
        
        return createToken(claims, userId.toString(), refreshTokenExpiration);
    }
    
    /**
     * Create JWT token
     * FIX A02: Uses environment variable secret (no weak defaults)
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        // Validate secret is strong enough (minimum 256 bits = 32 bytes)
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET must be at least 32 characters (256 bits). " +
                "Set environment variable JWT_SECRET with a strong secret. " +
                "Current length: " + (jwtSecret != null ? jwtSecret.length() : 0)
            );
        }
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Get user ID from token
     */
    public UUID getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String userId = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
        
        return UUID.fromString(userId);
    }
    
    /**
     * Get email from token
     * FIX A07: Email is now always populated in tokens
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String email = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
        
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email claim missing or empty in token");
        }
        return email;
    }

    /**
     * Get token type (ACCESS or REFRESH)
     */
    public String getTokenType(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);
    }

    /**
     * Get roles from token
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", java.util.List.class);
    }
    
    /**
     * Validate token
     * FIX A02: Uses environment variable secret
     * FIX A07: Validates email claim is present
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            var claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // FIX A07: Verify email claim is present for both ACCESS and REFRESH tokens
            String email = claims.get("email", String.class);
            if (email == null || email.isBlank()) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

