package com.websocket_hub.dto;

import lombok.Builder;

@Builder
public record RoomRequest(
        String type,

        String fromUserId,

        String toUserId,

        String roomId,

        String content
) implements Message<String> {
}
