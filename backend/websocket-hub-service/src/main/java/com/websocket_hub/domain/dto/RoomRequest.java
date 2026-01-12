package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

@Builder
public record RoomRequest(
        String roomName,

        RoomType roomType
) {
}
