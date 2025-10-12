package com.security_service.domain.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        Instant createdAt
) {
}
