package com.security_service.domain.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        Long id,

        String username,

        String role,

        String accessToken,

        String refreshToken
) {
}
