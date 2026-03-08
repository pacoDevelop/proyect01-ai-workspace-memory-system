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
 * @author GitHub Copilot / TASK-015
 */
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret:my-secret-key-for-jrecruiter-service-please-change-in-production}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration; // milliseconds (default 24h)
    
    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration; // milliseconds (default 7d)
    
    /**
     * Generate access token
     */
    public String generateAccessToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("type", "ACCESS");
        
        return createToken(claims, userId.toString(), jwtExpiration);
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("type", "REFRESH");
        
        return createToken(claims, userId.toString(), refreshTokenExpiration);
    }
    
    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
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
        String userId = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
        
        return UUID.fromString(userId);
    }
    
    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
