package com.game_service.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Validation error"),
    INVALID_MOVE("Invalid move"),
    INTERNAL_GAME_ERROR("Internal game error"),
    UNEXPECTED_ERROR("Unexpected error"),
    COOLDOWN("Cooldown"),

    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Not Found"),
    CONFLICT("Conflict"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again");

    private final String message;
}
