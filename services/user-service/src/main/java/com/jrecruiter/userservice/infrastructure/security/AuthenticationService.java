package com.jrecruiter.userservice.infrastructure.security;

import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.UserCredentialsJpaEntity;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories.UserCredentialsJpaRepository;
import com.jrecruiter.userservice.infrastructure.web.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles login, token refresh, and credential validation.
 */
@Service
public class AuthenticationService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialsJpaRepository repository;
    
    @Autowired
    public AuthenticationService(JwtTokenProvider jwtTokenProvider, 
                               AuthenticationManager authenticationManager,
                               UserCredentialsJpaRepository repository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.repository = repository;
    }
    
    /**
     * Authenticate and generate tokens
     */
    public AuthResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserCredentialsJpaEntity credentials = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

        java.util.List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(credentials.getUserId(), email, roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(credentials.getUserId(), email); // FIX A07: Pass email
        
        return new AuthResponse(accessToken, refreshToken, credentials.getUserId(), email);
    }
    
    /**
     * Refresh access token from refresh token
     */
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken); // FIX A07: Get email from token
        java.util.List<String> roles = jwtTokenProvider.getRolesFromToken(refreshToken);
        return jwtTokenProvider.generateAccessToken(userId, email, roles);
    }
}
