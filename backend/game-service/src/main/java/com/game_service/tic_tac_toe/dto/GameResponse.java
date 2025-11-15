package com.game_service.tic_tac_toe.dto;

import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record GameResponse(
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

        String message
) {
}
