package com.casualgames.gameservice.tic_tac_toe.domain.dto;

import com.casualgames.gameservice.common.enums.MessageType;
import com.casualgames.gameservice.tic_tac_toe.domain.enums.TicTacToeGameEvent;
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
