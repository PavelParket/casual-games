package com.security_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    MISSING_TOKEN("Missing token"),
    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Not found"),
    CONFLICT("Conflict"),
    INTERNAL_SERVER_ERROR("Unexpected server error"),
    SERVICE_UNAVAILABLE("Service unavailable. Please try later");

    private final String message;
}
