package com.jrecruiter.userservice.infrastructure.web.dto;

import java.util.UUID;

/**
 * Authentication Response DTO
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    UUID userId,
    String email
) {}
