package com.game_service.durak.domain.dto;

import com.game_service.durak.domain.enums.DurakStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameResponse(

        Long id,

        UUID roomId,

        List<DurakPlayerViewResponse> playerViews,

        Boolean isGameOver,

        UUID winnerId,

        DurakStatus status,

        List<UUID> players
) {
}
