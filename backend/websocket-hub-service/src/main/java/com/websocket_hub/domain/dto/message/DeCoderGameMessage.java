package com.websocket_hub.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.websocket_hub.domain.entity.DeCoderGameState;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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

        UUID winner
) implements Message<DeCoderGameEvent> {
}
