package com.websocket_hub.domain.dto.response;

import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RoomResponse(
        UUID id,

        String name,

        RoomType type,

        RoomStatus status,

        List<UUID> participantGuids,

        Integer participantCount
) {
}
