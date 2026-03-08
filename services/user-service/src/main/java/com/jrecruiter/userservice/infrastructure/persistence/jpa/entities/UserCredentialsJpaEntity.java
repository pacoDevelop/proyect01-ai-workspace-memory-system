package com.jrecruiter.userservice.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * User Credentials Entity
 * Stores authentication data independently from domain aggregates.
 */
@Entity
@Table(name = "user_credentials")
public class UserCredentialsJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "roles", nullable = false)
    private String roles; // Comma-separated roles (e.g. "CANDIDATE,ADMIN")

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public UserCredentialsJpaEntity() {}

    public UserCredentialsJpaEntity(UUID userId, String email, String passwordHash, Set<String> roles) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        setRoles(roles);
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Set<String> getRolesSet() {
        if (roles == null || roles.isBlank()) return new HashSet<>();
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public void setRoles(Set<String> rolesSet) {
        if (rolesSet == null || rolesSet.isEmpty()) {
            this.roles = "";
        } else {
            this.roles = String.join(",", rolesSet);
        }
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
