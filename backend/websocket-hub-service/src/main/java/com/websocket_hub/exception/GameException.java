package com.websocket_hub.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class GameException extends RuntimeException {

    private final String errorCode;

    private final UUID roomId;

    protected GameException(String errorCode, UUID roomId, String message) {
        super(message);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }

    protected GameException(String errorCode, UUID roomId, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }
}
