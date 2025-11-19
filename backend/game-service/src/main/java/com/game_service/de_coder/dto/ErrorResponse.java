package com.game_service.de_coder.dto;

import com.game_service.de_coder.enums.ErrorType;
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
