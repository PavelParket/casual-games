package com.websocket_hub.domain.dto.bank_service;

import lombok.Builder;

@Builder
public record TicTacToeTransactionInternalResponse(

        String status,

        String message,

        int transactionsCreated
) {
}
