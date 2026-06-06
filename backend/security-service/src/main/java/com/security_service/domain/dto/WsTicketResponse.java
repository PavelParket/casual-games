package com.security_service.domain.dto;

import lombok.Builder;

@Builder
public record WsTicketResponse(

        String ticketId
) {
}
