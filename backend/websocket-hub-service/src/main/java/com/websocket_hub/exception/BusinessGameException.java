package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.ErrorCode;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

public class BusinessGameException extends GameException {

    private BusinessGameException(ErrorCode errorCode, UUID roomId, String debugMessage) {
        super(errorCode, roomId, debugMessage);
    }

    public static BusinessGameException roomNotFound(UUID roomId) {
        return new BusinessGameException(
                ErrorCode.ROOM_NOT_FOUND,
                roomId,
                String.format("Room not found: roomId=%s", roomId)
        );
    }

    public static BusinessGameException invalidMove(UUID roomId) {
        return new BusinessGameException(
                ErrorCode.INVALID_MOVE,
                roomId,
                String.format("Invalid move in room: roomId=%s", roomId)
        );
    }

    public static BusinessGameException invalidMessage(UUID roomId, String payload) {
        return new BusinessGameException(
                ErrorCode.INVALID_MESSAGE,
                roomId,
                String.format("Invalid message: %s", payload)
        );
    }

    public static BusinessGameException gameNotStarted(UUID roomId) {
        return new BusinessGameException(
                ErrorCode.GAME_NOT_STARTED,
                roomId,
                String.format("Game-service returned null response on startGame: roomId=%s", roomId)
        );
    }

    public static BusinessGameException fromHttpResponse(UUID roomId, HttpClientErrorException e) {
        ErrorCode code = switch (e.getStatusCode().value()) {
            case 402 -> ErrorCode.INSUFFICIENT_BALANCE;
            case 404 -> ErrorCode.ROOM_NOT_FOUND;
            case 409 -> ErrorCode.INVALID_MOVE;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };

        return new BusinessGameException(
                code,
                roomId,
                String.format("Downstream service error (%s): %s", e.getStatusCode(), e.getResponseBodyAsString())
        );
    }
}
