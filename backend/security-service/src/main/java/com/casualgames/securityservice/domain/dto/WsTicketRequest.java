package com.casualgames.securityservice.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record WsTicketRequest(

        @NotNull
        UUID roomId
) {
}
