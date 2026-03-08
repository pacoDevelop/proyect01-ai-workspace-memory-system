package com.jrecruiter.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Token Refresh Request DTO
 */
public record RefreshRequest(
    @NotBlank String refreshToken
) {}
