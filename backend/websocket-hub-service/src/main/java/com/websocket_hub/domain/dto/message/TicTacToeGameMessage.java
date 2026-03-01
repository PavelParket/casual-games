package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.TicTacToeGameEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record TicTacToeGameMessage(

        MessageType type,

        TicTacToeGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        String[] board,

        Integer cell,

        String currentPlayerSymbol,

        String nextPlayerSymbol,

        Map<UUID, String> playersSymbols,

        Map<UUID, String> players,

        UUID winner,

        BigDecimal bet

) implements Message<TicTacToeGameEvent> {
}
