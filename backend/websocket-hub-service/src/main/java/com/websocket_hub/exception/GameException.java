package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GameException extends RuntimeException {

    private final ErrorCode errorCode;

    private final UUID roomId;

    public GameException(ErrorCode errorCode, UUID roomId) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.roomId = roomId;
    }

    public GameException(ErrorCode errorCode, UUID roomId, String debugMessage) {
        super(debugMessage);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }

    public GameException(ErrorCode errorCode, UUID roomId, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.roomId = roomId;
    }
}
