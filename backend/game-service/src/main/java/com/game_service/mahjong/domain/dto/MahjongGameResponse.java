package com.game_service.mahjong.domain.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MahjongGameResponse(

        UUID roomId,

        long seed,

        List<MahjongBoardResponse> boards,

        UUID playerGuid,

        boolean valid,

        List<String> removedSlotIds,

        int tilesRemaining,

        int availableMoves,

        boolean cleared,

        boolean deadlocked,

        UUID opponentGuid,

        int opponentTilesRemaining
) {
}
