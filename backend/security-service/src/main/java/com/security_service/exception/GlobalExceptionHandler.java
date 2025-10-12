package com.security_service.exception;

import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
import com.security_service.factory.ErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        log.warn("User not found: {}", e.getMessage());

        return new ResponseEntity<>(factory.create(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, e.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException e, HttpServletRequest request) {
        log.warn("Email already exists: {}", e.getMessage(), e);

        return new ResponseEntity<>(factory.create(HttpStatus.BAD_REQUEST, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(InvalidRoleException e, HttpServletRequest request) {
        log.warn("Invalid role: {}", e.getMessage(), e);

        return new ResponseEntity<>(factory.create(HttpStatus.BAD_REQUEST, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        log.warn("Validation errors: {}", details);

        return new ResponseEntity<>(factory.create(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, details), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());

        return new ResponseEntity<>(factory.create(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, e.getMessage(), request), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication failed: {}", e.getMessage());

        return new ResponseEntity<>(factory.create(HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_ERROR, e.getMessage(), request), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);

        return new ResponseEntity<>(factory.create(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", request), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
