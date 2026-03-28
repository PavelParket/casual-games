package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.DeCoderGameState;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;

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
