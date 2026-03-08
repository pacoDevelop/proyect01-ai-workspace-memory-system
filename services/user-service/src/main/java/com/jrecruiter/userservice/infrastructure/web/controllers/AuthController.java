package com.jrecruiter.userservice.infrastructure.web.controllers;

import com.jrecruiter.userservice.infrastructure.security.AuthenticationService;
import com.jrecruiter.userservice.infrastructure.web.dto.AuthResponse;
import com.jrecruiter.userservice.infrastructure.web.dto.LoginRequest;
import com.jrecruiter.userservice.infrastructure.web.dto.RefreshRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user login and session management.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.authenticate(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        String newAccessToken = authenticationService.refreshAccessToken(request.refreshToken());
        // For refresh, we only return the new access token in a partial response or the same format
        return ResponseEntity.ok(new AuthResponse(newAccessToken, request.refreshToken(), null, null));
    }
}
