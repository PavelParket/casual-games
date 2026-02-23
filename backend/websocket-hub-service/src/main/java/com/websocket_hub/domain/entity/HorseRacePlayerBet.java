package com.websocket_hub.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class HorseRacePlayerBet {

    private UUID guid;

    private Integer horseIndex;

    private Double odd;

    private BigDecimal amount;

    private BigDecimal balanceBefore;
}
