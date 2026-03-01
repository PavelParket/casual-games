package com.game_service.horse_race.domain.dto;

import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceGameRequest(

        HorseRaceEvent event,

        UUID roomId,

        Map<UUID, String> participants,

        Integer horseCount
) {
}
