package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // GAME — invalid game state or action
    VALIDATION_ERROR("Validation error"),
    INVALID_MOVE("This move is not allowed"),
    NOT_YOUR_TURN("It's not your turn"),
    GAME_NOT_STARTED("The game has not started yet"),
    GAME_ALREADY_FINISHED("The game has already finished"),
    ROOM_NOT_FOUND("Room not found"),
    ROOM_FULL("Room is full"),
    ROOM_ALREADY_EXISTS("A room with this name already exists"),
    ROOM_TYPE_NOT_FOUND("Unknown room type"),

    // BUSINESS — business rule violations
    INSUFFICIENT_BALANCE("Insufficient balance"),
    COOLDOWN("Too many requests. Please wait before trying again"),

    // PROTOCOL — malformed or unauthorized messages
    INVALID_MESSAGE("Invalid message format"),
    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Not Found"),
    CONFLICT("Conflict"),

    // SYSTEM — infrastructure failures
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again"),
    SERVICE_UNAVAILABLE("Service temporarily unavailable. Please try again later");

    private final String message;
}
