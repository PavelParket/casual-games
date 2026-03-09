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
    COOLDOWN("Cooldown");

    private final String message;
}
