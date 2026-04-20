package com.security_service.exception;

import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
import com.security_service.factory.ErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.security_service.config.ResourceMessageConstants.VALIDATION_FAILED;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorFactory factory;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.warn("Not found on {}: {}", request.getRequestURI(), e.getMessage(), e);

        return factory.create(ErrorCode.NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND, null, request);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConflict(ConflictException e, HttpServletRequest request) {
        log.warn("Conflict on {}: {}", request.getRequestURI(), e.getMessage(), e);

        return factory.create(ErrorCode.CONFLICT, e.getMessage(), HttpStatus.CONFLICT, null, request);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException e, HttpServletRequest request) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), e.getMessage(), e);

        return factory.create(ErrorCode.BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST, null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        String message = details.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(VALIDATION_FAILED);

        log.warn("Validation error on {}: {}", request.getRequestURI(), details);

        return factory.create(ErrorCode.BAD_REQUEST, message, HttpStatus.BAD_REQUEST, details, request);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(Exception e, HttpServletRequest request) {
        log.warn("Forbidden on {}: {}", request.getRequestURI(), e.getMessage());

        return factory.create(ErrorCode.FORBIDDEN, e.getMessage(), HttpStatus.FORBIDDEN, null, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication failed on {}: {}", request.getRequestURI(), e.getMessage());

        return factory.create(ErrorCode.UNAUTHORIZED, e.getMessage(), HttpStatus.UNAUTHORIZED, null, request);
    }

    @ExceptionHandler(MissingTokenException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErrorResponse handleMissingToken(MissingTokenException e, HttpServletRequest request) {
        log.warn("Missing token on {}: {}", request.getRequestURI(), e.getMessage());

        return factory.create(ErrorCode.MISSING_TOKEN, e.getMessage(), HttpStatus.NO_CONTENT, null, request);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceUnavailable(ServiceUnavailableException e, HttpServletRequest request) {
        log.error("Service unavailable on {}: {}", request.getRequestURI(), e.getMessage(), e);

        return factory.create(ErrorCode.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, null, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred on {}: {}", request.getRequestURI(), e.getMessage(), e);

        return factory.create(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null, request);
    }
}
