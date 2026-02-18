package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record HorseRaceMessage(

        MessageType type,

        HorseRaceEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Integer horseCount,

        double[] odds,

        String seedHash,

        String serverSeed,

        Integer winnerHorseIndex,

        Integer segmentsCount,

        List<double[]> ticks,

        Map<UUID, String> participants

) implements Message<HorseRaceEvent> {
}
