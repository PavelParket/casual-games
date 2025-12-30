package com.bank_service.domain.dto;

import lombok.Builder;

@Builder
public record ProcessingResultResponse(
        String status,

        String message,

        int transactionsCreated
) {
}
