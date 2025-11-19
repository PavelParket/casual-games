package com.game_service.de_coder.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    VALIDATION_ERROR("Validation error"),
    INVALID_MOVE("Invalid move"),
    COOLDOWN("Cooldown"),
    INTERNAL_GAME_ERROR("Internal game error"),
    UNEXPECTED_ERROR("Unexpected error");

    private final String description;
}
