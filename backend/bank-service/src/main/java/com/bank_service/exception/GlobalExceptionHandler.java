package com.bank_service.exception;

import com.bank_service.domain.dto.ErrorResponse;
import com.bank_service.domain.enums.ErrorCode;
import com.bank_service.factory.ErrorResponseFactory;
import com.security_starter.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorResponseFactory factory;

    @ExceptionHandler({
            UnsupportedRoomTypeException.class,
            UnsupportedFactoryTypeException.class,
            BetsNotFoundException.class,
            PlayerNotFoundException.class,
            BusinessValidationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestExceptions(RuntimeException e, HttpServletRequest request) {
        log.warn("Bad Request Exception: {}", e.getMessage());

        ErrorCode code = switch (e.getClass().getSimpleName()) {
            case "UnsupportedRoomTypeException", "UnsupportedFactoryTypeException" -> ErrorCode.UNSUPPORTED_TYPE;
            default -> ErrorCode.RESOURCE_NOT_FOUND;
        };

        return factory.create(code, e.getMessage(), HttpStatus.BAD_REQUEST, request, null);
    }

    @ExceptionHandler(ClientInternalRequestException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceRequestException(ClientInternalRequestException e, HttpServletRequest request) {
        log.error("Service Dependency Error: {}", e.getMessage());

        return factory.create(ErrorCode.SERVICE_DEPENDENCY_ERROR, e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.warn("Forbidden: {}", e.getMessage());
        return factory.create(ErrorCode.FORBIDDEN, e.getMessage(), HttpStatus.FORBIDDEN, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        org.springframework.validation.FieldError::getField,
                        Collectors.mapping(org.springframework.validation.FieldError::getDefaultMessage, Collectors.toList())
                ));

        String message = e.getBindingResult().getFieldError() != null ?
                e.getBindingResult().getFieldError().getDefaultMessage() :
                "Validation failed";

        log.warn("Validation Error: {}", message);

        return factory.create(ErrorCode.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST, request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Invalid request parameters");

        log.warn("Constraint Violation: {}", message);

        return factory.create(ErrorCode.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST, request, null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("Internal Server Error: Unexpected exception occurred.", e);

        return factory.create(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                null
        );
    }
}
