package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.entity.DeCoderGameState;
import com.casualgames.websockethub.domain.enums.events.DeCoderGameEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DeCoderGameInternalResponse(
        DeCoderGameEvent event,

        UUID roomId,

        String message,

        UUID player,

        UUID winner,

        BigDecimal jackpot,

        List<DeCoderGameState> gameState,

        Boolean isGameStarted
) {
}
