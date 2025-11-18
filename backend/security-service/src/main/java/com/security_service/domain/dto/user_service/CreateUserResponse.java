package com.security_service.domain.dto.user_service;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record CreateUserResponse(
        Long id,

        String username,

        String email,

        BigDecimal balance,

        String role,

        String status,

        Instant createdAt
) {
}
