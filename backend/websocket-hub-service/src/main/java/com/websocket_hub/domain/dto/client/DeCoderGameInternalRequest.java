package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.enums.events.DeCoderGameEvent;

import java.util.UUID;

public record DeCoderGameInternalRequest(
        DeCoderGameEvent event,

        UUID roomId,

        String code,

        UUID player
) {
}
