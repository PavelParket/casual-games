package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.entity.MahjongTile;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.MahjongGameEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record MahjongGameMessage(

        MessageType type,

        MahjongGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        Long seed,

        List<MahjongTile> tiles,

        Map<UUID, String> players,

        String slot1,

        String slot2,

        List<String> removedSlotIds,

        Integer tilesRemaining,

        Integer availableMoves,

        Integer seconds,

        UUID winner,

        BigDecimal bet

) implements Message<MahjongGameEvent> {
}
