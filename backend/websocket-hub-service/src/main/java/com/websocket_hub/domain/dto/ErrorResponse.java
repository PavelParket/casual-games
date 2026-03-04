package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.ErrorCode;

import java.time.Instant;

public record ErrorResponse(

        ErrorCode errorCode,

        String message,

        Instant timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message, Instant.now());
    }
}
