package com.casualgames.websockethub.domain.dto.response;

import com.casualgames.websockethub.domain.enums.RoomStatus;
import com.casualgames.websockethub.domain.enums.RoomType;
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
