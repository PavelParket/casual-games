package com.websocket_hub.handler;

import com.websocket_hub.domain.context.WebSocketContext;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.ErrorCategory;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.manager.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketErrorHandler {

    private final SessionManager sessionManager;

    public void handle(WebSocketContext webSocketContext, Exception exception, boolean forceClose) {
        ErrorCode code = resolveErrorCode(exception);
        ErrorCategory category = code.getCategory();
        String message = resolveMessage(code, category, exception);

        log(exception, code, category, webSocketContext);

        sendErrorToClient(webSocketContext, code, message);

        if (forceClose || ErrorCategory.SYSTEM.equals(category)) {
            closeSession(webSocketContext.session(), CloseStatus.SERVER_ERROR);
        }
    }

    private ErrorCode resolveErrorCode(Exception e) {
        if (e instanceof GameException gameException) {
            return gameException.getErrorCode();
        }

        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(ErrorCode code, ErrorCategory category, Exception e) {
        return switch (category) {
            case GAME, BUSINESS -> e.getMessage();
            case SYSTEM -> code.getMessage();
        };
    }

    private void log(Exception exception, ErrorCode errorCode, ErrorCategory category, WebSocketContext context) {
        switch (category) {
            case GAME -> log.warn(
                    "Game error: errorCode={}, roomId={}, message={}",
                    errorCode, context.roomId(), exception.getMessage()
            );
            case BUSINESS -> log.warn(
                    "Business error: errorCode={}, userId={}, message={}",
                    errorCode, context.user().guid(), exception.getMessage()
            );
            case SYSTEM ->
                    log.error("System error: errorCode={}, userId={}", errorCode, context.user().guid(), exception);
        }
    }

    private void sendErrorToClient(WebSocketContext context, ErrorCode errorCode, String clientMessage) {
        UserInternalResponse user = context.user();

        if (user == null || user.guid() == null) {
            log.warn("Cannot send error — user is null: errorCode={}", errorCode);
            return;
        }

        ClientSession clientSession = sessionManager.getByGuid(user.guid());

        if (clientSession == null) {
            log.warn("Cannot send error — client session not found: userId={}, errorCode={}", user.guid(), errorCode);
            return;
        }

        ErrorMessage errorMessage = ErrorMessage.builder()
                .type(MessageType.SYSTEM)
                .event(ErrorEvent.ERROR)
                .toUserId(user.guid())
                .roomId(context.roomId())
                .message(clientMessage)
                .build();

        sessionManager.sendToSession(clientSession, errorMessage);

        log.info("Error sent to client: userId={}, roomId={}, errorCode={}", user.guid(), context.roomId(), errorCode);
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
