package com.game_service.horse_race.domain.dto;

import com.game_service.common.enums.MessageType;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record HorseRaceRequest(

        MessageType type,

        HorseRaceEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Map<UUID, String> participants,

        Integer horseCount,

        BigDecimal bet,

        Integer horseIndex
) {
}
