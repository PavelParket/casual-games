package com.casualgames.gameservice.durak.domain.dto;

import com.casualgames.gameservice.durak.domain.entity.DurakCard;
import com.casualgames.gameservice.durak.domain.enums.DurakAction;
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
