package com.game_service.tic_tac_toe.factory;

import com.game_service.tic_tac_toe.dto.ErrorResponse;
import com.game_service.tic_tac_toe.enums.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ErrorFactory implements Factory<ErrorResponse> {

    @Override
    public ErrorResponse create(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Expected arguments: error, message, HttpStatus");
        }

        ErrorType error = (ErrorType) args[0];
        String message = (String) args[1];
        HttpStatus status = (HttpStatus) args[2];

        return create(error, message, status);
    }

    private ErrorResponse create(ErrorType error, String message, HttpStatus status) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(Instant.now())
                .build();
    }
}
