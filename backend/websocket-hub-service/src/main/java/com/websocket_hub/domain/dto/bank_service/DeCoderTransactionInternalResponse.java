package com.websocket_hub.domain.dto.bank_service;

import lombok.Builder;

@Builder
public record DeCoderTransactionInternalResponse(

        String status,

        String message
) {
}
