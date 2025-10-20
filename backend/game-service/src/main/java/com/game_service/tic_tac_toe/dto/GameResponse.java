package com.game_service.tic_tac_toe.dto;

import com.game_service.tic_tac_toe.enums.MessageType;
import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record GameResponse(
        MessageType type,

        String roomName,

        String[][] board,

        Integer cell,

        String player,

        String nextPlayer,

        Map<String, String> playersSymbols,

        Set<String> players,

        String winner,

        String message
) {
}
