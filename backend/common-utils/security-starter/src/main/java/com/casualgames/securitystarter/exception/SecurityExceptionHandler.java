package com.casualgames.securitystarter.exception;

import com.casualgames.commonutils.dto.ErrorResponse;
import com.casualgames.commonutils.enums.ErrorCode;
import com.casualgames.commonutils.exception.JwtException;
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

import static com.casualgames.securitystarter.config.ResourceMessageConstants.FORBIDDEN;
import static com.casualgames.securitystarter.config.ResourceMessageConstants.UNAUTHORIZED;

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
