package com.websocket_hub.handler;

import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.JwtException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
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
        UUID userGuid = null;

        if (webSocketContext.user() != null) {
            userGuid = webSocketContext.user().guid();
        }

        log(exception, code, category, webSocketContext);

        ErrorMessage errorMessage = buildErrorMessage(
                webSocketContext.roomId(),
                userGuid,
                code,
                category,
                message
        );

        webSocketHelper.sendToSession(userGuid, errorMessage);

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

            case BUSINESS -> log.warn("Business error: errorCode={}, userId={}, message={}", errorCode,
                    context.user() != null ? context.user().guid() : "unknown", exception.getMessage());

            case PROTOCOL -> log.warn("Protocol error: errorCode={}, userId={}, message={}", errorCode,
                    context.user() != null ? context.user().guid() : "unknown", exception.getMessage());

            case SYSTEM -> log.error("System error: errorCode={}, userId={}", errorCode,
                    context.user() != null ? context.user().guid() : "unknown", exception);
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
