package com.game_service.horse_race.domain.entity;

import com.game_service.common.enums.MessageType;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;

import java.util.UUID;

public record HorseRacePreset(

        MessageType type,

        HorseRaceEvent event,

        UUID roomId,

        String message,

        int horsesCount,

        double[] odds
) {
}
