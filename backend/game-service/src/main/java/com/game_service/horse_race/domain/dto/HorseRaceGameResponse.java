package com.game_service.horse_race.domain.dto;

import com.game_service.horse_race.domain.entity.HorseRaceHorseKeyframes;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record HorseRaceGameResponse(

        Long id,

        UUID roomId,

        String serverSeed,

        String seedHash,

        Integer horseCount,

        List<Double> odds,

        Integer winnerHorseIndex,

        Integer segmentsCount,

        List<HorseRaceHorseKeyframes> horseKeyframes
) {
}
