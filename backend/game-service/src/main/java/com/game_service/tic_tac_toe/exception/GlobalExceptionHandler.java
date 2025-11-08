package com.game_service.tic_tac_toe.exception;

import com.game_service.tic_tac_toe.dto.ErrorResponse;
import com.game_service.tic_tac_toe.enums.ErrorType;
import com.game_service.tic_tac_toe.factory.ErrorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorFactory factory;

    @ExceptionHandler(GameValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(GameValidationException e) {
        log.warn("Validation error: {}", e.getMessage());

        return factory.create(ErrorType.VALIDATION_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMoveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidMove(InvalidMoveException e) {
        log.warn("Invalid move: {}", e.getMessage());

        return factory.create(ErrorType.INVALID_MOVE, e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GameInternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternal(GameInternalException e) {
        log.error("Internal game error: {}", e.getMessage());

        return factory.create(ErrorType.INTERNAL_GAME_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception e) {
        log.error("Unexpected error", e);

        return factory.create(ErrorType.UNEXPECTED_ERROR, "Unexpected server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
