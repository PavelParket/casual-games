package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.DurakCard;
import com.websocket_hub.domain.enums.model.DurakAction;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameInternalRequest(

        Long id,

        UUID roomId,

        List<UUID> players,

        UUID currentActorId,

        DurakAction action,

        DurakCard card,

        UUID winnerId

) {
}
