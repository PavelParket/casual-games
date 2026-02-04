package com.bank_service.domain.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record TransactionSummaryResponse(

        Long id,

        UUID userGuid,

        BigDecimal balanceBefore,

        BigDecimal balanceAfter,

        BigDecimal totalWon,

        BigDecimal totalLost,

        BigDecimal netProfit,

        LocalDate summaryMonth
) {
}
