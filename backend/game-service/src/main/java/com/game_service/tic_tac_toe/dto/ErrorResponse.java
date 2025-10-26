package com.game_service.tic_tac_toe.dto;

import com.game_service.tic_tac_toe.enums.ErrorType;
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
