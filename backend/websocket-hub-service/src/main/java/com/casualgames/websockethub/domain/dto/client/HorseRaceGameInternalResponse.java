package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.entity.HorseRaceHorseKeyframes;
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

        List<HorseRaceHorseKeyframes> horseKeyframes
) {
}
