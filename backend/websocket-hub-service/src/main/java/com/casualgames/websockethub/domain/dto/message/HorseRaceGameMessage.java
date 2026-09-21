package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.entity.HorseRaceHorseKeyframes;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.HorseRaceEvent;
import lombok.Builder;

import java.math.BigDecimal;
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

        List<HorseRaceHorseKeyframes> horseKeyframes,

        Integer horseIndex,

        BigDecimal bet,

        Integer remainingSeconds

) implements Message<HorseRaceEvent> {
}
