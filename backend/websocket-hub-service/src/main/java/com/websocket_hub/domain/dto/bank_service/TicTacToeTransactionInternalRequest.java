package com.websocket_hub.domain.dto.bank_service;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TicTacToeTransactionInternalRequest(

        UUID roomId,

        List<PlayerBet> playerBets,

        UUID winner
) {
}
