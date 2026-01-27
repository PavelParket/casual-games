package com.websocket_hub.domain.dto;

import lombok.Builder;

@Builder
public record RoomTypeResponse(
        String name,

        String label,

        String handlerUrl
) {
}
