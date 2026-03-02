package com.websocket_hub.domain.dto.client;

import lombok.Builder;

@Builder
public record TicTacToeTransactionInternalResponse(

        String status,

        String message,

        int transactionsCreated
) {
}
