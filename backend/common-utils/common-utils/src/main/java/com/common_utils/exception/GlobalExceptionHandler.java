package com.common_utils.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation failed: path={}", request.getRequestURI());

        return ErrorResponse.of(e, request);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        log.warn("Bad request on: {}", e.getMessage());

        return ErrorResponse.of(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, e.getMessage(), null, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.warn("Forbidden on: {}", e.getMessage());

        return ErrorResponse.of(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, e.getMessage(), null, request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        log.warn("Not found on: {}", e.getMessage());

        return ErrorResponse.of(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, e.getMessage(), null, request);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e, HttpServletRequest request) {
        log.warn("Conflict on: {}", e.getMessage());

        return ErrorResponse.of(ErrorCode.CONFLICT, HttpStatus.CONFLICT, e.getMessage(), null, request);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceUnavailableException(ServiceUnavailableException e, HttpServletRequest request) {
        log.error("Service unavailable on: {}", e.getMessage(), e);

        return ErrorResponse.of(ErrorCode.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE.getMessage(), null, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("Internal server error on: {}", e.getMessage(), e);

        return ErrorResponse.of(request);
    }
}
