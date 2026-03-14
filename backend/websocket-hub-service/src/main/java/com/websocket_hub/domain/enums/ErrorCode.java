package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // GAME — invalid game state or action
    VALIDATION_ERROR("Validation error", ErrorCategory.GAME),
    INVALID_MOVE("This move is not allowed", ErrorCategory.GAME),
    NOT_YOUR_TURN("It's not your turn", ErrorCategory.GAME),
    GAME_NOT_STARTED("The game has not started yet", ErrorCategory.GAME),
    GAME_ALREADY_FINISHED("The game has already finished", ErrorCategory.GAME),
    ROOM_NOT_FOUND("Room not found", ErrorCategory.GAME),
    ROOM_FULL("Room is full", ErrorCategory.GAME),
    ROOM_ALREADY_EXISTS("A room with this name already exists", ErrorCategory.GAME),
    ROOM_TYPE_NOT_FOUND("Unknown room type", ErrorCategory.GAME),

    // BUSINESS — business rule violations
    INSUFFICIENT_BALANCE("Insufficient balance", ErrorCategory.BUSINESS),
    COOLDOWN("Too many requests. Please wait before trying again", ErrorCategory.BUSINESS),

    // PROTOCOL — malformed or unauthorized messages
    INVALID_MESSAGE("Invalid message format", ErrorCategory.PROTOCOL),
    BAD_REQUEST("Bad Request", ErrorCategory.PROTOCOL),
    UNAUTHORIZED("Unauthorized", ErrorCategory.PROTOCOL),
    FORBIDDEN("Forbidden", ErrorCategory.PROTOCOL),
    NOT_FOUND("Not Found", ErrorCategory.PROTOCOL),
    CONFLICT("Conflict", ErrorCategory.PROTOCOL),

    // SYSTEM — infrastructure failures
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again", ErrorCategory.SYSTEM),
    SERVICE_UNAVAILABLE("Service temporarily unavailable. Please try again later", ErrorCategory.SYSTEM);

    private final String message;
    private final ErrorCategory category;
}
