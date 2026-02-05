package com.security_service.exception;

import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
import com.security_service.factory.ErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorFactory factory;

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        log.warn("User not found: {}", e.getMessage(), e);

        return factory.create(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmailExists(EmailAlreadyExistsException e, HttpServletRequest request) {
        log.warn("Email already exists: {}", e.getMessage(), e);

        return factory.create(HttpStatus.BAD_REQUEST, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request);
    }

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRole(InvalidRoleException e, HttpServletRequest request) {
        log.warn("Invalid role: {}", e.getMessage(), e);

        return factory.create(HttpStatus.BAD_REQUEST, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        log.warn("Validation errors: {}", details);

        return factory.create(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());

        return factory.create(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, e.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication failed: {}", e.getMessage());

        return factory.create(HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
        log.warn("Authentication failed due to invalid credentials: {}", e.getMessage());

        return factory.create(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, e.getMessage(), request);
    }

    @ExceptionHandler(MissingTokenException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErrorResponse handleMissingToken(MissingTokenException e, HttpServletRequest request) {
        log.warn("Missing token: {}", e.getMessage());

        return factory.create(HttpStatus.NO_CONTENT, ErrorCode.MISSING_TOKEN, e.getMessage(), request);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceUnavailable(ServiceUnavailableException e, HttpServletRequest request) {
        log.error("Service unavailable: {}", e.getMessage(), e);

        return factory.create(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE, e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);

        return factory.create(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", request);
    }
}
