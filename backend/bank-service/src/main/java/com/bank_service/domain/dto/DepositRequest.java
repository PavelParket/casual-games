package com.bank_service.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositRequest(
        @NotNull(message = "Player GUID cannot be null")
        UUID userGuid,

        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        BigDecimal amount
) {}
