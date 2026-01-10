package com.websocket_hub.domain.dto.message;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Builder
public record TicTacToeGameMessage(

        String type,

        String event,

        String fromUserId,

        String toUserId,

        String roomName,

        String[] board,

        Integer cell,

        String player,

        String nextPlayer,

        Map<String, String> playersSymbols,

        Set<String> players,

        String winner,

        BigDecimal bet,

        String message
) implements Message {
}
