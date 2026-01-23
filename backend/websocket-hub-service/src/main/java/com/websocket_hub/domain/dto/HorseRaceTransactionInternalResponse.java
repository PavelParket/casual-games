package com.websocket_hub.domain.dto.client;

import lombok.Builder;

@Builder
public record HorseRaceTransactionInternalResponse(

        String status,

        String message,

        int transactionsCreated
) {
}
