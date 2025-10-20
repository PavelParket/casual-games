package com.game_service.tic_tac_toe.exception;

import com.game_service.tic_tac_toe.dto.ErrorResponse;
import com.game_service.tic_tac_toe.enums.ErrorType;
import com.game_service.tic_tac_toe.factory.ErrorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorFactory factory;

    @ExceptionHandler(GameValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(GameValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        return new ResponseEntity<>(factory.create(ErrorType.VALIDATION_ERROR, ex.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMove(InvalidMoveException ex) {
        log.warn("Invalid move: {}", ex.getMessage());

        return new ResponseEntity<>(factory.create(ErrorType.INVALID_MOVE, ex.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GameInternalException.class)
    public ResponseEntity<ErrorResponse> handleInternal(GameInternalException ex) {
        log.error("Internal game error: {}", ex.getMessage());

        return new ResponseEntity<>(factory.create(ErrorType.INTERNAL_GAME_ERROR, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        return new ResponseEntity<>(factory.create(ErrorType.UNEXPECTED_ERROR, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
