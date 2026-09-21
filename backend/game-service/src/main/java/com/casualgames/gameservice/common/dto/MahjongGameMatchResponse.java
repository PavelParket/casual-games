package com.casualgames.gameservice.common.dto;

import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record MahjongGameMatchResponse(

    Long id,

    GameType gameType,

    UUID roomId,

    GameResult gameResult,

    UUID winnerId,

    List<UUID> players,

    Instant createdAt
) implements GameMatchResponse {
}
