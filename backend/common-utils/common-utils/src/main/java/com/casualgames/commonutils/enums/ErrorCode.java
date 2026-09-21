package com.casualgames.commonutils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please slow down."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service is unavailable now. Please try later"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again"),

    NO_SESSION(HttpStatus.UNAUTHORIZED, "No active session found"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token is invalid or expired"),
    SESSION_REVOKED(HttpStatus.UNAUTHORIZED, "Session has been revoked");

    private final HttpStatus httpStatus;
    private final String message;

    public static ErrorCode fromStatus(HttpStatus status) {
        return Stream.of(values())
                .filter(code -> code.httpStatus.equals(status))
                .findFirst()
                .orElse(INTERNAL_SERVER_ERROR);
    }
}
