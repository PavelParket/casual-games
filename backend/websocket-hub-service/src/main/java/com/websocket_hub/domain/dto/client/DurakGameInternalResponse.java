package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.enums.model.DurakStatus;
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
