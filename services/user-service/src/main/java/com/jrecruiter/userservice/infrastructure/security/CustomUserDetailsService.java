package com.jrecruiter.userservice.infrastructure.security;

import com.jrecruiter.userservice.infrastructure.persistence.jpa.entities.UserCredentialsJpaEntity;
import com.jrecruiter.userservice.infrastructure.persistence.jpa.repositories.UserCredentialsJpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom User Details Service
 * Bridges Spring Security with the UserCredentials persistence layer.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialsJpaRepository repository;

    public CustomUserDetailsService(UserCredentialsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentialsJpaEntity credentials = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = credentials.getRolesSet().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new User(credentials.getEmail(), credentials.getPasswordHash(), 
                        credentials.isActive(), true, true, true, authorities);
    }
}
