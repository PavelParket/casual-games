package com.casualgames.gameservice.common.dto;

import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record HorseRaceGameMatchResponse(

        Long id,

        GameType gameType,

        UUID roomId,

        GameResult gameResult,

        Integer winnerHorseIndex,

        Integer horseCount,

        Instant createdAt

) implements GameMatchResponse {
}
