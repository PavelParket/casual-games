package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_MOVE("This move is not allowed"),
    NOT_YOUR_TURN("It's not your turn"),
    ROOM_NOT_FOUND("Room not found"),
    ROOM_FULL("Room is full"),
    GAME_NOT_STARTED("The game has not started yet"),
    GAME_ALREADY_FINISHED("The game has already finished"),
    INSUFFICIENT_BALANCE("Insufficient balance"),
    ROOM_ALREADY_EXISTS("A room with this name already exists"),
    ROOM_TYPE_NOT_FOUND("Unknown room type"),

    SERVICE_UNAVAILABLE("Service temporarily unavailable. Please try again later"),
    INTERNAL_ERROR("An unexpected error occurred. Please try again"),
    INVALID_MESSAGE("Invalid message format");

    private final String message;
}
