package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.entity.HorseRaceGameTick;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceGameMessage(

        MessageType type,

        HorseRaceEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Map<UUID, String> participants,

        Integer horseCount,

        List<Double> odds,

        String seedHash,

        String serverSeed,

        Integer winnerHorseIndex,

        Integer segmentsCount,

        List<HorseRaceGameTick> ticks

) implements Message<HorseRaceEvent> {
}
