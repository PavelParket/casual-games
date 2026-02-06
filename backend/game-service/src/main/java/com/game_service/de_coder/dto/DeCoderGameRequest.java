package com.game_service.de_coder.dto;

import com.game_service.de_coder.enums.DeCoderGameEvent;
import com.game_service.de_coder.enums.MessageType;
import java.util.UUID;

public record DeCoderGameRequest(
        MessageType type,

        DeCoderGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Integer code,

        UUID player,

        UUID winner
) {
}
