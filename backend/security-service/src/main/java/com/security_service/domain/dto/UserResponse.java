package com.security_service.domain.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID guid,
        String username,
        String email,
        String role,
        Instant createdAt
) {
}
