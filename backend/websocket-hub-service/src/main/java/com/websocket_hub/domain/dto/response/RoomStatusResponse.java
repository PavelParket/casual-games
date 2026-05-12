package com.websocket_hub.domain.dto.response;

import com.websocket_hub.domain.enums.RoomStatus;
import lombok.Builder;

@Builder
public record RoomStatusResponse(

        RoomStatus roomStatus
) {
}
