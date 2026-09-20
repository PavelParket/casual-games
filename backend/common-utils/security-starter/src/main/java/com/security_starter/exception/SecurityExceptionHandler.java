package com.security_starter.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import com.common_utils.exception.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.security_starter.config.ResourceMessageConstants.FORBIDDEN;
import static com.security_starter.config.ResourceMessageConstants.UNAUTHORIZED;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SecurityExceptionHandler {

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwtException(JwtException ex, HttpServletRequest request) {
        log.warn("JWT exception: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.UNAUTHORIZED, UNAUTHORIZED, request.getRequestURI(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication exception: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.UNAUTHORIZED, UNAUTHORIZED, request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied exception: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.FORBIDDEN, FORBIDDEN, request.getRequestURI(), null);
    }
}
