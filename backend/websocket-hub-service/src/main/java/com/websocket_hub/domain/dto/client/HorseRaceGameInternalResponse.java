package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.HorseRaceGameTick;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record HorseRaceGameInternalResponse(

        UUID roomId,

        Integer horseCount,

        List<Double> odds,

        String serverSeed,

        String seedHash,

        Integer winnerHorseIndex,

        Integer segmentsCount,

        List<HorseRaceGameTick> ticks
) {
}
