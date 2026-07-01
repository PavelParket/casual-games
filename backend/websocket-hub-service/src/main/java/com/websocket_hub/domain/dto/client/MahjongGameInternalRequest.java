package com.websocket_hub.domain.dto.client;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MahjongGameInternalRequest(

        UUID roomId,

        List<UUID> players,

        UUID playerGuid,

        String slot1,

        String slot2
) {
}
