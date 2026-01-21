package com.websocket_hub.domain.dto.game_service;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PlayerInternalRequest(

        UUID guid,

        String username,

        String email
) {
}
