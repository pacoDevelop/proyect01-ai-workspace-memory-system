package com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories;

import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.UserCredentialsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialsJpaRepository extends JpaRepository<UserCredentialsJpaEntity, UUID> {
    Optional<UserCredentialsJpaEntity> findByEmail(String email);
}
