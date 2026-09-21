package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.enums.model.DurakStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameInternalResponse(

        Long id,

        UUID roomId,

        List<DurakPlayerViewResponse> playerViews,

        Boolean isGameOver,

        UUID winnerId,

        DurakStatus status,

        List<UUID> players
) {
}
