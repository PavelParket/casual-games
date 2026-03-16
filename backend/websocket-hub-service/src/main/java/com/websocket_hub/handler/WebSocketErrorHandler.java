package com.websocket_hub.handler;

import com.websocket_hub.domain.context.WebSocketContext;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.ErrorCategory;
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
        ErrorCategory category = code.getCategory();
        String message = resolveMessage(code, category, exception);

        log(exception, code, category, webSocketContext);

        ErrorMessage errorMessage = buildErrorMessage(
                webSocketContext.roomId(),
                webSocketContext.user().guid(),
                code,
                category,
                message
        );

        webSocketHelper.sendToSession(webSocketContext.user().guid(), errorMessage);

        if (forceClose || category == ErrorCategory.PROTOCOL) {
            CloseStatus closeStatus = forceClose ? CloseStatus.SERVER_ERROR : CloseStatus.POLICY_VIOLATION;
            closeSession(webSocketContext.session(), closeStatus);
        }
    }

    public void handle(UUID roomId, Set<ClientSession> clients, Exception exception) {
        ErrorCode code = resolveErrorCode(exception);
        ErrorCategory category = code.getCategory();
        String message = resolveMessage(code, category, exception);

        log.warn("Room error: errorCode={}, roomId={}, message={}", code, roomId, exception.getMessage());

        ErrorMessage errorMessage = buildErrorMessage(roomId, null, code, category, message);

        webSocketHelper.broadcastToSessions(clients, errorMessage);
    }

    private ErrorMessage buildErrorMessage(UUID roomId,
                                           UUID toUserId,
                                           ErrorCode code,
                                           ErrorCategory category,
                                           String message) {
        return ErrorMessage.builder()
                .type(MessageType.SYSTEM)
                .event(ErrorEvent.ERROR)
                .toUserId(toUserId)
                .roomId(roomId)
                .message(message)
                .errorCode(code)
                .errorCategory(category)
                .timestamp(Instant.now())
                .build();
    }

    private ErrorCode resolveErrorCode(Exception exception) {
        if (exception instanceof GameException e) {
            return e.getErrorCode();
        }

        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(ErrorCode code, ErrorCategory category, Exception e) {
        return switch (category) {
            case GAME, BUSINESS -> e.getMessage();
            case PROTOCOL, SYSTEM -> code.getMessage();
        };
    }

    private void log(Exception exception, ErrorCode errorCode, ErrorCategory category, WebSocketContext context) {
        switch (category) {
            case GAME ->
                    log.warn("Game error: errorCode={}, roomId={}, message={}", errorCode, context.roomId(), exception.getMessage());

            case BUSINESS ->
                    log.warn("Business error: errorCode={}, userId={}, message={}", errorCode, context.user().guid(), exception.getMessage());

            case SYSTEM ->
                    log.error("System error: errorCode={}, userId={}", errorCode, context.user().guid(), exception);
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
