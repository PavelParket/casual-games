package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.entity.DeCoderGameState;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.DeCoderGameEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record DeCoderGameMessage(

        MessageType type,

        DeCoderGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        boolean isGameStarted,

        List<DeCoderGameState> gameState,

        BigDecimal jackpot,

        String message,

        String code,

        UUID player,

        UUID winner,

        BigDecimal balanceBefore,

        BigDecimal spent

) implements Message<DeCoderGameEvent> {
}
