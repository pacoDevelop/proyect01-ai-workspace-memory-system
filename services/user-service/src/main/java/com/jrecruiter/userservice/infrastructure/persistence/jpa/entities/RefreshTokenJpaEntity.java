package com.jrecruiter.userservice.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefreshTokenJpaEntity — Persists refresh tokens for rotation and revocation
 * Enables secure refresh token management with expiration and revocation tracking.
 * 
 * @author GitHub Copilot / TASK-015
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_token_hash", columnList = "token_hash")
})
public class RefreshTokenJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash; // SHA-256 hash of actual token (security: don't store plaintext)
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt; // Null if not revoked; set on revocation
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ============ Constructors ============
    
    public RefreshTokenJpaEntity() {}
    
    public RefreshTokenJpaEntity(UUID userId, String tokenHash, String email, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.email = email;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.revokedAt = null;
    }
    
    // ============ Getters & Setters ============
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getTokenHash() {
        return tokenHash;
    }
    
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // ============ Helper Methods ============
    
    /**
     * Check if token is valid (not revoked and not expired)
     */
    public boolean isValid() {
        return revokedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Revoke this token
     */
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "RefreshTokenJpaEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", revokedAt=" + revokedAt +
                '}';
    }
}
