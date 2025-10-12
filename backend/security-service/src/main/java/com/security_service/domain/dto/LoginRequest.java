package com.security_service.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email cannot be empty")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
