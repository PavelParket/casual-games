package com.security_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Validation error"),
    AUTHENTICATION_ERROR("Authentication failed"),
    INVALID_CREDENTIALS("Invalid credentials"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Resource not found"),
    CONFLICT("Resource already exists"),
    INTERNAL_SERVER_ERROR("Unexpected server error"),
    SERVICE_UNAVAILABLE("Service unavailable"),
    MISSING_TOKEN("Missing authentication token");

    private final String message;
}
