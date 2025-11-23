package com.security_service.domain.dto.user_service;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateUserInternalResponse(
        UUID guid,

        String username,

        String email,

        BigDecimal balance,

        String role,

        String status,

        Instant createdAt
) {
}
