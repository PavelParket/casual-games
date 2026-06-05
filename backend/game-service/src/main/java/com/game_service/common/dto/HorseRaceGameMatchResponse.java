package com.game_service.common.dto;

import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
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
