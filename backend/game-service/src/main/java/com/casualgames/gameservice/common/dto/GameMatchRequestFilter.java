package com.casualgames.gameservice.common.dto;

import com.casualgames.gameservice.common.enums.GameType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GameMatchRequestFilter(

        @NotNull(message = "Game type is required")
        GameType gameType,

        Boolean isWinner
) {
}
