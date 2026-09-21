package com.casualgames.gameservice.de_coder.domain.dto;

import com.casualgames.gameservice.de_coder.domain.entity.DeCoderGameState;
import com.casualgames.gameservice.de_coder.domain.enums.DeCoderGameEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
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

        List<DeCoderGameState> gameState,

        Boolean isGameStarted
) {
}
