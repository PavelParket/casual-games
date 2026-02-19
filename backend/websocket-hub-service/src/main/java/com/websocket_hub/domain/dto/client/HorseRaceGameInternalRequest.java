package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceGameInternalRequest(

        HorseRaceEvent event,

        UUID roomId,

        Map<UUID, String> participants,

        Integer horseCount
) {
}
