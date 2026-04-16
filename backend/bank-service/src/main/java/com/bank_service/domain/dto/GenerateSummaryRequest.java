package com.bank_service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GenerateSummaryRequest(

        @NotNull(message = "Target month cannot be null")
        LocalDate targetMonth
) {
}
