package com.websocket_hub.domain.dto.client;

import lombok.Builder;

@Builder
public record DeCoderTransactionInternalResponse(

        String status,

        String message
) {
}
