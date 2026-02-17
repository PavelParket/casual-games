package com.game_service.horse_race.domain.dto;

import com.game_service.common.enums.MessageType;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;

import java.util.UUID;

public record HorseRacePresetResponse(

        MessageType type,

        HorseRaceEvent event,

        UUID roomId,

        String message,

        int horseCount,

        double[] odds
) {
}
