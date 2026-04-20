package com.bank_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionSummaryResponse {

    private Long id;

    private UUID userGuid;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private BigDecimal totalWon;

    private BigDecimal totalLost;

    private BigDecimal netProfit;

    private LocalDate summaryMonth;
}
