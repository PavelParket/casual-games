package com.websocket_hub.domain.entity;

import com.websocket_hub.domain.enums.model.SpendingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class DecoderPlayerSpending {

    private UUID userGuid;

    private BigDecimal balanceBefore;

    @Setter
    private BigDecimal spent;

    @Setter
    private SpendingType type;
}
