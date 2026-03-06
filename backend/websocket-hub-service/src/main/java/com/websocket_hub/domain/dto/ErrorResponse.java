package com.websocket_hub.domain.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Builder
public record ErrorResponse(

        HttpStatus status,

        String message,

        Instant timestamp
) {
}
