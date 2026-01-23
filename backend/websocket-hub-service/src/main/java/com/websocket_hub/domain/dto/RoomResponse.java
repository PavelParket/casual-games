package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RoomResponse(
        UUID id,

        String name,

        RoomType type,

        List<UUID> participantGuids,

        Integer participantCount
) {
}
