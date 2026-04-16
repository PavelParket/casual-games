package com.bank_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Validation error"),
    UNSUPPORTED_TYPE("Unsupported entity or room type"),
    RESOURCE_NOT_FOUND("Required resource or player not found"),
    FORBIDDEN("Forbidden"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again later"),
    SERVICE_DEPENDENCY_ERROR("Service dependency failed to respond");

    private final String message;
}
