package com.security_service.domain.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponse(
        UUID guid,

        String username,

        String email,

        String role,

        String accessToken
) {
}
