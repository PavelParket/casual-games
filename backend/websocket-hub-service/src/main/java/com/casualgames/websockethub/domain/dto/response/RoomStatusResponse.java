package com.casualgames.websockethub.domain.dto.response;

import com.casualgames.websockethub.domain.enums.RoomStatus;
import lombok.Builder;

@Builder
public record RoomStatusResponse(

        RoomStatus roomStatus
) {
}
