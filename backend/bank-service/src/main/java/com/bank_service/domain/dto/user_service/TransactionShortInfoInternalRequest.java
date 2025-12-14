package com.bank_service.domain.dto.user_service;

import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TransactionShortInfoInternalRequest(
        Long id,

        UUID userGuid,

        BigDecimal amount,

        TransactionType type,

        TransactionStatus status
) {
}
