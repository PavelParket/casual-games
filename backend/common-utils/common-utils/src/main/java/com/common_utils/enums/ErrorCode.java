package com.common_utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Not Found"),
    CONFLICT("Conflict"),
    TOO_MANY_REQUESTS("Too many requests. Please slow down."),
    SERVICE_UNAVAILABLE("Service is unavailable now. Please try later"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again"),

    NO_SESSION("No active session found"),
    INVALID_TOKEN("Token is invalid or expired"),
    SESSION_REVOKED("Session has been revoked");

    private final String message;
}
