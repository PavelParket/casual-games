package com.bank_service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record TransactionSummaryFilterRequest(

        @NotNull(message = "User guid cannot be null!")
        UUID userGuid,

        @NotNull(message = "Start month cannot be null!")
        LocalDate startDate,

        LocalDate endDate
) {
}
