package com.game_service.de_coder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
@JsonInclude(NON_NULL)
public record DeCoderGameResponse(
        DeCoderGameEvent event,

        UUID roomId,

        String message,

        UUID player,

        UUID winner,

        BigDecimal jackpot,

        List<GameState> gameState,

        Boolean isGameStarted
) {
    public record GameState(Integer code, Integer exactMatch, Integer partialMatch) {}
}
