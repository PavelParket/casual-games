package com.bank_service.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositRequest(
        @NotNull
        UUID userGuid,

        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount
) {}
