package com.websocket_hub.domain.dto.client;

import lombok.Builder;

@Builder
public record DurakTransactionInternalResponse(

        String status,

        String message,

        int transactionsCreated
) {
}
