package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.enums.events.DeCoderGameEvent;

import java.util.UUID;

public record DeCoderGameInternalRequest(
        DeCoderGameEvent event,

        UUID roomId,

        String code,

        UUID player
) {
}
