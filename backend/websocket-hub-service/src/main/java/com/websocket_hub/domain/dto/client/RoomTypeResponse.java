package com.websocket_hub.domain.dto.client;

import lombok.Builder;

@Builder
public record RoomTypeResponse(
        String name,

        String label,

        String handlerUrl
) {
}
