package com.game_service.horse_race.domain.dto;

import com.game_service.common.enums.MessageType;
import com.game_service.horse_race.domain.entity.HorseTick;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record HorseRaceResponse(

        MessageType type,

        HorseRaceEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        String seedHash,

        String serverSeed,

        Integer horseCount,

        double[] odds,

        Integer winnerHorseIndex,

        Integer segmentsCount,

        List<HorseTick> ticks
) {
}
