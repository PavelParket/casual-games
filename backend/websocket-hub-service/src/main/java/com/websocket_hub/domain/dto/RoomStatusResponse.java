package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.RoomStatus;
import lombok.Builder;

@Builder
public record RoomStatusResponse(

        RoomStatus roomStatus
) {
}
