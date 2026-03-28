package com.game_service.durak.domain.dto;

import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.enums.DurakAction;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameRequest(

        Long id,

        UUID roomId,

        List<UUID> players,

        UUID currentActorId,

        DurakAction action,

        DurakCard card,

        UUID winnerId

) {
}
