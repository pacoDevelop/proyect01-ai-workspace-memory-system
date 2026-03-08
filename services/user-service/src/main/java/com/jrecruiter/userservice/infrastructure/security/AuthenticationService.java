package com.jrecruiter.userservice.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Authentication Service
 * Handles login, token refresh, and credential validation.
 * 
 * @author GitHub Copilot / TASK-015
 */
@Service
public class AuthenticationService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordHashingService passwordHashingService;
    
    @Autowired
    public AuthenticationService(JwtTokenProvider jwtTokenProvider, 
                               PasswordHashingService passwordHashingService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordHashingService = passwordHashingService;
    }
    
    /**
     * Authenticate and generate tokens
     */
    public AuthenticationResponse authenticate(UUID userId, String email, String password, String storedHash) {
        if (!passwordHashingService.verifyPassword(password, storedHash)) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String accessToken = jwtTokenProvider.generateAccessToken(userId, email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        
        return new AuthenticationResponse(accessToken, refreshToken, userId);
    }
    
    /**
     * Refresh access token from refresh token
     */
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        return jwtTokenProvider.generateAccessToken(userId, "");
    }
    
    /**
     * Authentication Response DTO
     */
    public static class AuthenticationResponse {
        private final String accessToken;
        private final String refreshToken;
        private final UUID userId;
        
        public AuthenticationResponse(String accessToken, String refreshToken, UUID userId) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public UUID getUserId() { return userId; }
    }
}
