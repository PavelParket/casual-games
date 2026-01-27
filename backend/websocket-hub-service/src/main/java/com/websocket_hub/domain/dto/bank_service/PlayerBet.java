package com.websocket_hub.domain.dto.bank_service;

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

    private UUID guid;

    private BigDecimal bet;

    private BigDecimal balanceBefore;
}
