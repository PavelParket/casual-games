package com.security_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Validation error"),
    AUTHENTICATION_ERROR("Authentication failed"),
    ACCESS_DENIED("Access denied"),
    NOT_FOUND("Resource not found"),
    INTERNAL_ERROR("Unexpected server error"),
    MISSING_TOKEN("Missing authentication token");

    private final String message;
}
