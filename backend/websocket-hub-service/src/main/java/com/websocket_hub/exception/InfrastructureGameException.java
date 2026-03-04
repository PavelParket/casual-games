package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;

import java.util.UUID;

public class InfrastructureGameException extends GameException {

    public InfrastructureGameException(UUID roomId, String debugMessage) {
        super(ErrorCode.SERVICE_UNAVAILABLE.getCode(), roomId, debugMessage);
    }

    public InfrastructureGameException(UUID roomId, String debugMessage, Throwable cause) {
        super(ErrorCode.SERVICE_UNAVAILABLE.getCode(), roomId, debugMessage, cause);
    }
}
