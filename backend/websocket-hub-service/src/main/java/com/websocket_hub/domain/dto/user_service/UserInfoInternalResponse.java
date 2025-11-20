package com.websocket_hub.domain.dto.user_service;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserInfoInternalResponse(
        UUID guid,

        String username,

        String email,

        String role,

        String status
) {
}
