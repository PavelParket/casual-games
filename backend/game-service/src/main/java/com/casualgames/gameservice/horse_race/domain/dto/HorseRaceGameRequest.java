package com.casualgames.gameservice.horse_race.domain.dto;

import com.casualgames.gameservice.horse_race.domain.enums.HorseRaceEvent;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceGameRequest(

        HorseRaceEvent event,

        UUID roomId,

        Map<UUID, Integer> participants,

        Integer horseCount
) {
}
