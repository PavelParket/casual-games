package com.game_service.de_coder.dto;

import com.game_service.de_coder.enums.DeCoderGameEvent;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeCoderGameRequest(
        DeCoderGameEvent event,

        UUID roomId,

        String code,

        UUID player
) {
}
