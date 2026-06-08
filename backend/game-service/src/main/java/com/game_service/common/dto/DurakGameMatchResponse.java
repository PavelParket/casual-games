package com.game_service.common.dto;

import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameMatchResponse(

        Long id,

        GameType gameType,

        UUID roomId,

        GameResult gameResult,

        UUID winnerId,

        List<UUID> players,

        Instant createdAt

) implements GameMatchResponse {
}
