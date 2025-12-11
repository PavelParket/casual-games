package com.bank_service.domain.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerBet {

    @NotNull(message = "Player GUID cannot be null")
    private UUID guid;

    @NotNull(message = "Bet amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bet must be greater than 0")
    private BigDecimal bet;

    @NotNull(message = "Balance before cannot be null")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balanceBefore;
}
