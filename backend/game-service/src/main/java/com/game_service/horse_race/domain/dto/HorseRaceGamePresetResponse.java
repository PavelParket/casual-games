package com.game_service.horse_race.domain.dto;

import java.util.List;
import java.util.UUID;

public record HorseRaceGamePresetResponse(

        UUID roomId,

        Integer horseCount,

        List<Double> odds
) {
}
