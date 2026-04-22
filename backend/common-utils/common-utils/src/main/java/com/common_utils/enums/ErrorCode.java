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
    SERVICE_UNAVAILABLE("Service is unavailable now. Please try later"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again");

    private final String message;
}
