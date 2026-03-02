package com.game_service.de_coder.dto;

import com.game_service.common.enums.MessageType;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeCoderGameRequest(
        MessageType type,

        DeCoderGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Integer code,

        UUID player,

        UUID winner,

        String gameState,

        Boolean isGameStarted
) {
}
