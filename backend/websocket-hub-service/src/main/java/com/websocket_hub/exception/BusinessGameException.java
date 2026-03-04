package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;

import java.util.UUID;

public class BusinessGameException extends GameException {

    public BusinessGameException(ErrorCode errorCode, UUID roomId) {
        super(errorCode.getCode(), roomId, errorCode.getCode());
    }

    public BusinessGameException(ErrorCode errorCode, UUID roomId, String debugMessage) {
        super(errorCode.getCode(), roomId, debugMessage);
    }
}
