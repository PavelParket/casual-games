package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.RoomType;

import java.util.List;
import java.util.UUID;

public record RoomInfoResponse(
        UUID id,

        String name,

        RoomType type,

        List<String> participantEmails,

        Integer participantCount
) {
}
