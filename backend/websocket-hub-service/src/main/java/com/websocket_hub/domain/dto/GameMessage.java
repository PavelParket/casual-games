package com.websocket_hub.domain.dto;

import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record GameMessage(
        String type,

        String fromUserId,

        String toUserId,

        String roomId,

        String[][] board,

        Integer cell,

        String player,

        String nextPlayer,

        Map<String, String> playersSymbols,

        Set<String> players,

        String winner,

        String message
) implements Message<String> {
    @Override
    public String content() {
        return message;
    }
}
