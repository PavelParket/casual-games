package com.websocket_hub.domain.dto.user_service;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UserInfoInternalResponse(

        UUID guid,

        String username,

        String email,

        BigDecimal balance,

        String role,

        String status
) {
}
