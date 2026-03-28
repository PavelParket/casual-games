package com.game_service.de_coder.domain.dto;

import com.game_service.de_coder.domain.enums.DeCoderGameEvent;
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
