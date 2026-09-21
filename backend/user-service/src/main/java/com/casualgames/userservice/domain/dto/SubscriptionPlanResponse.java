package com.casualgames.userservice.domain.dto;

import com.casualgames.securitystarter.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubscriptionPlanResponse(

        Long id,

        Status status,

        BigDecimal price,

        BigDecimal upgradePrice,

        Integer tier
) {
}
