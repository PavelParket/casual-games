package com.security_service.domain.dto.client;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Deprecated(forRemoval = true)
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
