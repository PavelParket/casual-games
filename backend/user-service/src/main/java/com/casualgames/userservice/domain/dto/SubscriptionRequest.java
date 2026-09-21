package com.casualgames.userservice.domain.dto;

import com.casualgames.securitystarter.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubscriptionRequest(

        @NotNull(message = "Status cannot be null")
        Status status
) {
}
