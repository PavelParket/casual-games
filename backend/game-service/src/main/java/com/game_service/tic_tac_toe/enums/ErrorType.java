package com.game_service.tic_tac_toe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    VALIDATION_ERROR("Validation error"),
    INVALID_MOVE("Invalid move"),
    INTERNAL_GAME_ERROR("Internal game error"),
    UNEXPECTED_ERROR("Unexpected error");

    private final String description;
}
