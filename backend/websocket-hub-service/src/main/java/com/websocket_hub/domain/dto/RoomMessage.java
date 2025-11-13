package com.websocket_hub.domain.dto;

import lombok.Builder;

@Builder
public record RoomMessage(
        String type,

        String event,

        String fromUserId,

        String toUserId,

        String roomName,

        String message
) implements Message {
}
