package com.websocket_hub.domain.dto.client;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UserInternalResponse(

        UUID guid,

        String username,

        String email,

        BigDecimal balance,

        String role,

        String status
) {
}
