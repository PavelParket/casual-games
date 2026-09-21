package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.entity.MahjongBoard;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MahjongGameInternalResponse(

        UUID roomId,

        long seed,

        List<MahjongBoard> boards,

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
