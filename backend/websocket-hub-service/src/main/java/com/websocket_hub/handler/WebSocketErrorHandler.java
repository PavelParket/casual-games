package com.websocket_hub.handler;

import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.JwtException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
import com.websocket_hub.domain.context.WebSocketContext;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.helper.WebSocketHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketErrorHandler {

    private final WebSocketHelper webSocketHelper;

    public void handle(WebSocketContext webSocketContext, Exception exception, boolean forceClose) {
        ErrorCode code = resolveErrorCode(exception);
        String message = resolveMessage(code, exception);
        UUID userGuid = null;

        if (webSocketContext.user() != null) {
            userGuid = webSocketContext.user().guid();
        }

        log(exception, code, webSocketContext);

        ErrorMessage errorMessage = buildErrorMessage(
                webSocketContext.roomId(),
                userGuid,
                code,
                message
        );

        webSocketHelper.sendToSession(userGuid, errorMessage);

        if (forceClose || shouldCloseConnection(code)) {
            CloseStatus closeStatus = forceClose ? CloseStatus.SERVER_ERROR : CloseStatus.POLICY_VIOLATION;
            closeSession(webSocketContext.session(), closeStatus);
        }
    }

    public void handle(UUID roomId, Set<ClientSession> clients, Exception exception) {
        ErrorCode code = resolveErrorCode(exception);
        String message = resolveMessage(code, exception);

        log.warn("Room error: errorCode={}, roomId={}, message={}", code, roomId, exception.getMessage());

        ErrorMessage errorMessage = buildErrorMessage(roomId, null, code, message);

        webSocketHelper.broadcastToSessions(clients, errorMessage);
    }

    private ErrorMessage buildErrorMessage(UUID roomId, UUID toUserId, ErrorCode code, String message) {
        return ErrorMessage.builder()
                .type(MessageType.SYSTEM)
                .event(ErrorEvent.ERROR)
                .toUserId(toUserId)
                .roomId(roomId)
                .message(message)
                .errorCode(code)
                .timestamp(Instant.now())
                .build();
    }

    private ErrorCode resolveErrorCode(Exception exception) {
        return switch (exception) {
            case GameException e -> e.getErrorCode();
            case ForbiddenException e -> ErrorCode.FORBIDDEN;
            case NotFoundException e -> ErrorCode.NOT_FOUND;
            case BadRequestException e -> ErrorCode.BAD_REQUEST;
            case JwtException e -> ErrorCode.UNAUTHORIZED;
            case ServiceUnavailableException e -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    private String resolveMessage(ErrorCode code, Exception e) {
        return (e instanceof GameException) ? e.getMessage() : code.getMessage();
    }

    private boolean shouldCloseConnection(ErrorCode code) {
        return switch (code) {
            case UNAUTHORIZED, FORBIDDEN -> true;
            default -> false;
        };
    }

    private void log(Exception exception, ErrorCode code, WebSocketContext context) {
        String userId = context.user() != null ? context.user().guid().toString() : "unknown";

        switch (code) {
            case INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE ->
                    log.error("System error: errorCode={}, userId={}, roomId={}", code, userId, context.roomId(), exception);
            default ->
                    log.warn("WS error: errorCode={}, userId={}, roomId={}, message={}", code, userId, context.roomId(), exception.getMessage());
        }
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (Exception e) {
            log.warn("Failed to close session {}: {}", session.getId(), e.getMessage());
        }
    }
}
