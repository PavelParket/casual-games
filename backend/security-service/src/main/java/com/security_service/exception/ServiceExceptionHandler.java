package com.security_service.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ServiceExceptionHandler {

    @ExceptionHandler(MissingRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleMissingRefreshToken(MissingRefreshTokenException e, HttpServletRequest request) {
        log.warn("Missing refresh token: path={}", request.getRequestURI());
        return ErrorResponse.of(ErrorCode.NO_SESSION, HttpStatus.UNAUTHORIZED, e.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException e, HttpServletRequest request) {
        log.warn("Invalid refresh token: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED, e.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(SessionRevokedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleSessionRevoked(SessionRevokedException e, HttpServletRequest request) {
        log.warn("Session revoked: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.SESSION_REVOKED, HttpStatus.UNAUTHORIZED, e.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleExpiredCredentials(CredentialsExpiredException e, HttpServletRequest request) {
        log.warn("Credentials expired: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, e.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.warn("Bad credentials: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, e.getMessage(), null, request.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUsernameNotFound(UsernameNotFoundException e, HttpServletRequest request) {
        log.warn("User not found: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, e.getMessage(), null, request.getRequestURI());
    }
}
