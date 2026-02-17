package com.game_service.common.dto;

import com.game_service.common.enums.ErrorType;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Builder
public record ErrorResponse(
        ErrorType error,

        String message,

        HttpStatus status,

        Instant timestamp
) {
}
