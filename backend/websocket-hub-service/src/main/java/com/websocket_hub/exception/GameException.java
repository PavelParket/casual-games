package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GameException extends RuntimeException {

    private final ErrorCode errorCode;

    private final UUID roomId;

    protected GameException(ErrorCode errorCode, UUID roomId, String message) {
        super(message);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }

    protected GameException(ErrorCode errorCode, UUID roomId, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }

    protected static String extractMessage(Throwable e) {
        if (e == null) {
            return "unknown error";
        }

        if (e.getMessage() != null && !e.getMessage().endsWith(": null")) {
            return e.getMessage();
        }

        Throwable cause = e.getCause();

        if (cause != null && cause.getMessage() != null) {
            return String.format("%s: %s", cause.getClass().getSimpleName(), cause.getMessage());
        }

        return String.format("%s (no message)", e.getClass().getSimpleName());
    }
}
