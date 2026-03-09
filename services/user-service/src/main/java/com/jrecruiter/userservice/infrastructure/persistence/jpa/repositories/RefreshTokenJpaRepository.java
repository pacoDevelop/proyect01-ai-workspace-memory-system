package com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories;

import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshTokenJpaRepository — Spring Data JPA repository for RefreshTokenJpaEntity
 * Manages persistence and queries for refresh tokens.
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    
    /**
     * Find a valid refresh token by hash (not revoked, not expired)
     */
    @Query("SELECT rt FROM RefreshTokenJpaEntity rt " +
           "WHERE rt.tokenHash = :tokenHash " +
           "AND rt.revokedAt IS NULL " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    Optional<RefreshTokenJpaEntity> findValidByTokenHash(@Param("tokenHash") String tokenHash);
    
    /**
     * Find all valid tokens for a user
     */
    @Query("SELECT rt FROM RefreshTokenJpaEntity rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.revokedAt IS NULL " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    List<RefreshTokenJpaEntity> findValidByUserId(@Param("userId") UUID userId);
    
    /**
     * Find all tokens for a user (including revoked/expired)
     */
    List<RefreshTokenJpaEntity> findByUserId(UUID userId);
    
    /**
     * Delete all tokens for a user
     */
    void deleteByUserId(UUID userId);
}
