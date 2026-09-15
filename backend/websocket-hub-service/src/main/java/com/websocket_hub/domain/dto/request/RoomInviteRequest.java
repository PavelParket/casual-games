package com.websocket_hub.domain.dto.request;

import com.websocket_hub.domain.enums.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RoomInviteRequest(

        @NotNull(message = "Room id is required")
        UUID roomId,

        @NotNull(message = "Room type is required")
        RoomType roomType,

        @NotNull(message = "Friend guid is required")
        UUID friendGuid
) {
}
