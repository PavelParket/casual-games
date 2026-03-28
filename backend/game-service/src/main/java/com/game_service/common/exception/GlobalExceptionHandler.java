package com.game_service.common.exception;

import com.game_service.common.dto.ErrorResponse;
import com.game_service.common.enums.ErrorCode;
import com.game_service.common.factory.ErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.game_service.config.ResourceMessageConstants.COOLDOWN_WAIT_SECONDS;
import static com.game_service.config.ResourceMessageConstants.UNEXPECTED_SERVER_ERROR;
import static com.game_service.config.ResourceMessageConstants.UNREADABLE_REQUEST_BODY;
import static com.game_service.config.ResourceMessageConstants.VALIDATION_FAILED;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorFactory factory;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        String message = details.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(VALIDATION_FAILED);

        log.warn("Validation error on {}: {}", request.getRequestURI(), details);

        return ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .details(details)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("Malformed request on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(UNREADABLE_REQUEST_BODY)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.warn("Not found on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(GameValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(GameValidationException e) {
        log.warn("Validation error: {}", e.getMessage());

        return factory.create(ErrorCode.VALIDATION_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMoveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidMove(InvalidMoveException e) {
        log.warn("Invalid move: {}", e.getMessage());

        return factory.create(ErrorCode.INVALID_MOVE, e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GameInternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternal(GameInternalException e) {
        log.error("Internal game error: {}", e.getMessage());

        return factory.create(ErrorCode.INTERNAL_GAME_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        return factory.create(ErrorCode.UNEXPECTED_ERROR, UNEXPECTED_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CooldownException.class)
    public ErrorResponse handleCooldown(CooldownException ex) {

        long remainingTimeSec = (long) Math.ceil(ex.getRemainingTimeMs() / 1000.0);

        log.warn("Cooldown error. Returning 429. Retry-After: {}s", remainingTimeSec);

        HttpHeaders httpheaders = new HttpHeaders();
        httpheaders.set(HttpHeaders.RETRY_AFTER, String.valueOf(remainingTimeSec));

        return factory.create(
                ErrorCode.COOLDOWN,
                String.format(COOLDOWN_WAIT_SECONDS, remainingTimeSec),
                httpheaders,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
