package com.game_service.common.factory;

import com.game_service.common.dto.ErrorResponse;
import com.game_service.common.enums.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ErrorFactory implements Factory<ErrorResponse> {

    @Override
    public ErrorResponse create(Object... args) {
        if (args.length < 3
                || !(args[0] instanceof ErrorType error)
                || !(args[1] instanceof String message)
                || !(args[2] instanceof HttpStatus status)
        ) {
            throw new IllegalArgumentException("Expected arguments: error, message, HttpStatus");
        }

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
