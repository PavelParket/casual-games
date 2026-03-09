package com.websocket_hub.exception;

import com.websocket_hub.domain.dto.ErrorResponse;
import com.websocket_hub.domain.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(Exception e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(Exception e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.UNAUTHORIZED)
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(Exception e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.FORBIDDEN)
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(Exception e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(Exception e) {
        log.warn(e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.CONFLICT)
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .timestamp(Instant.now())
                .build();
    }
}
