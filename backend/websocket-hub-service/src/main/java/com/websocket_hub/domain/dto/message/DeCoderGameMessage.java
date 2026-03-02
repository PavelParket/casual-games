package com.websocket_hub.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.domain.enums.MessageType;
import lombok.Builder;

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

        String gameState,

        String message,

        Integer code,

        UUID player,

        UUID winner
) implements Message<DeCoderGameEvent> {
}
