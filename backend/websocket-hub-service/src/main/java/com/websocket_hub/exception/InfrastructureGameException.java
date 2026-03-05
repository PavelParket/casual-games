package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;

import java.util.UUID;

public class InfrastructureGameException extends GameException {

    private InfrastructureGameException(UUID roomId, String debugMessage) {
        super(ErrorCode.SERVICE_UNAVAILABLE, roomId, debugMessage);
    }

    private InfrastructureGameException(UUID roomId, String debugMessage, Throwable cause) {
        super(ErrorCode.SERVICE_UNAVAILABLE, roomId, debugMessage, cause);
    }

    public static InfrastructureGameException gameServiceUnavailable(String operation, UUID roomId, Throwable cause) {
        return new InfrastructureGameException(
                roomId,
                String.format("Game-service unavailable on %s: %s", operation, extractMessage(cause)),
                cause
        );
    }

    public static InfrastructureGameException bankServiceUnavailable(String operation, UUID roomId, Throwable cause) {
        return new InfrastructureGameException(
                roomId,
                String.format("Bank-service unavailable on %s: %s", operation, extractMessage(cause)),
                cause
        );
    }

    public static InfrastructureGameException bankServiceUnexpectedStatus(String operation, UUID roomId, int status) {
        return new InfrastructureGameException(
                roomId,
                String.format("Bank-service returned unexpected status %s on %s", status, operation)
        );
    }

    public static InfrastructureGameException bankServiceNullResponse(String operation, UUID roomId) {
        return new InfrastructureGameException(
                roomId,
                String.format("Bank-service returned null response on %s", operation)
        );
    }
}
