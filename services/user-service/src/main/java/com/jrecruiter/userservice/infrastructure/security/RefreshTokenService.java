package com.jrecruiter.userservice.infrastructure.security;

import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.RefreshTokenJpaEntity;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories.RefreshTokenJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * RefreshTokenService — Manages refresh token lifecycle with rotation and revocation
 * Implements secure refresh token handling with hashing, rotation, and revocation.
 * 
 * Strategy:
 * - Store token hashes (not plaintext) for security
 * - Implement token rotation: old tokens are invalidated on refresh
 * - Track token lifecycle: issued, expires, revoked timestamps
 * 
 * @author GitHub Copilot / TASK-015
 */
@Service
public class RefreshTokenService {
    
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private static final int TOKEN_LIFETIME_DAYS = 7;
    
    @Autowired
    public RefreshTokenService(RefreshTokenJpaRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    /**
     * Create and persist a new refresh token
     */
    @Transactional
    public RefreshTokenJpaEntity createRefreshToken(UUID userId, String token, String email) {
        String tokenHash = hashToken(token);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(TOKEN_LIFETIME_DAYS);
        
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
            userId,
            tokenHash,
            email,
            now,
            expiresAt
        );
        
        return refreshTokenRepository.save(entity);
    }
    
    /**
     * Validate refresh token: check if it exists, is not revoked, and not expired
     */
    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String token) {
        String tokenHash = hashToken(token);
        return refreshTokenRepository.findValidByTokenHash(tokenHash).isPresent();
    }
    
    /**
     * Get refresh token details if valid
     */
    @Transactional(readOnly = true)
    public RefreshTokenJpaEntity getValidRefreshToken(String token) {
        String tokenHash = hashToken(token);
        return refreshTokenRepository.findValidByTokenHash(tokenHash)
            .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
    }
    
    /**
     * Rotate refresh token: invalidate old token and create new one
     * This ensures one-time use of refresh tokens for enhanced security
     */
    @Transactional
    public RefreshTokenJpaEntity rotateRefreshToken(String oldToken, UUID userId, String newToken, String email) {
        // Revoke old token
        RefreshTokenJpaEntity oldTokenEntity = getValidRefreshToken(oldToken);
        oldTokenEntity.revoke();
        refreshTokenRepository.save(oldTokenEntity);
        
        // Create new token
        return createRefreshToken(userId, newToken, email);
    }
    
    /**
     * Revoke a specific refresh token
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        String tokenHash = hashToken(token);
        refreshTokenRepository.findValidByTokenHash(tokenHash)
            .ifPresent(entity -> {
                entity.revoke();
                refreshTokenRepository.save(entity);
            });
    }
    
    /**
     * Revoke all refresh tokens for a user (logout all sessions)
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }
    
    /**
     * Delete all refresh tokens for a user (permanent deletion)
     */
    @Transactional
    public void deleteAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    /**
     * Hash token using SHA-256 for secure storage
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
