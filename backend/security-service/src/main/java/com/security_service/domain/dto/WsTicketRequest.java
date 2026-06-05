package com.security_service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record WsTicketRequest(

        @NotNull
        UUID roomId
) {
}
