package com.game_service.common.dto;

import com.game_service.common.enums.GameType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GameMatchRequestFilter(

        @NotNull(message = "Game type is required")
        GameType gameType,

        Boolean isWinner
) {
}
