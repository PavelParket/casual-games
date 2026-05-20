package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;
import lombok.Getter;

@Getter
public class GameException extends RuntimeException {

    private final ErrorCode errorCode;

    public GameException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GameException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GameException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
