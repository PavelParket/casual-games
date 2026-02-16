package com.game_service.tic_tac_toe.dto;

import com.game_service.common.enums.MessageType;
import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record TicTacToeGameRequest(

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

        UUID winner
) {
}
