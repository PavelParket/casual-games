package com.websocket_hub.domain.dto;

import lombok.Builder;

@Builder
public record RoomResponse(
        String type,

        String fromUserId,

        String toUserId,

        String roomId,

        String content
) implements Message<String> {
}
