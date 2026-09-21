package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.enums.events.HorseRaceEvent;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceGameInternalRequest(

        HorseRaceEvent event,

        UUID roomId,

        Map<UUID, Integer> participants,

        Integer horseCount
) {
}
