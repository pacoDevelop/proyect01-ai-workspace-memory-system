package com.jrecruiter.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 */
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
