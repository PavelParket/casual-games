package com.casualgames.securityservice.domain.dto;

import lombok.Builder;

@Builder
public record WsTicketResponse(

        String ticketId
) {
}
