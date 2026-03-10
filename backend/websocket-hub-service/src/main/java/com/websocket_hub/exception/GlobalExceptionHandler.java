package com.websocket_hub.exception;

import com.websocket_hub.domain.dto.ErrorResponse;
import com.websocket_hub.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

import static com.websocket_hub.config.ResourceMessageConstants.UNREADABLE_REQUEST_BODY;
import static com.websocket_hub.config.ResourceMessageConstants.VALIDATION_FAILED;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException e, HttpServletRequest request) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.warn("Unauthorized on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.UNAUTHORIZED)
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e, HttpServletRequest request) {
        log.warn("Forbidden on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.FORBIDDEN)
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
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

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e, HttpServletRequest request) {
        log.warn("Conflict on {}: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .errorCode(ErrorCode.CONFLICT)
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), e.getMessage(), e);
        return ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
    }
}
