package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Business errors
    INVALID_MOVE("INVALID_MOVE"),
    NOT_YOUR_TURN("NOT_YOUR_TURN"),
    ROOM_NOT_FOUND("ROOM_NOT_FOUND"),
    ROOM_FULL("ROOM_FULL"),
    GAME_NOT_STARTED("GAME_NOT_STARTED"),
    GAME_ALREADY_FINISHED("GAME_ALREADY_FINISHED"),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE"),

    // Infrastructure / system errors
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    INVALID_MESSAGE("INVALID_MESSAGE");

    private final String code;
}
