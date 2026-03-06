package com.websocket_hub.handler;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public abstract class AppWebSocketHandler<T extends AbstractRoomManager> extends TextWebSocketHandler {

    protected final SessionManager sessionManager;

    protected final T roomManager;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        UserInternalResponse user = null;
        UUID roomId = null;

        try {
            user = WebSocketUtil.getUser(session);
            roomId = WebSocketUtil.getRoomId(session);
            Instant connectedAt = WebSocketUtil.getConnectedAt(session);

            sessionManager.register(user.guid(), user, session, connectedAt);
            roomManager.addSession(roomId, user, session);

            onJoin(roomId, user);

            log.info("Connection established: userId={}, roomId={}", user.guid(), roomId);

        } catch (GameException e) {
            log.warn("Game error on connect: errorCode={}, roomId={}, message={}", e.getErrorCode(), e.getRoomId(), e.getMessage());
            sendError(user, e.getRoomId() != null ? e.getRoomId() : roomId, session, e.getErrorCode(), e.getMessage());
            closeSession(session, CloseStatus.POLICY_VIOLATION);

        } catch (Exception e) {
            log.error("Unexpected error on connect: session={}", session.getId(), e);
            sendError(user, roomId, session, ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
            closeSession(session, CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        try {
            UserInternalResponse user = WebSocketUtil.getUser(session);
            UUID roomId = WebSocketUtil.getRoomId(session);

            roomManager.removeSession(roomId, user, session);
            sessionManager.remove(user.guid());

            onLeave(roomId, user);

            log.info("Connection closed: userId={}, roomId={}, status={}", user.guid(), roomId, status);
        } catch (Exception e) {
            log.warn("Error during connection cleanup for session {}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public final void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        UserInternalResponse user = null;
        UUID roomId = null;

        try {
            user = WebSocketUtil.getUser(session);
            roomId = WebSocketUtil.getRoomId(session);

            handleMessage(session, message);

        } catch (GameException e) {
            log.warn("Game error: errorCode={}, roomId={}, message={}", e.getErrorCode(), e.getRoomId(), e.getMessage());
            sendError(
                    user,
                    e.getRoomId() != null ? e.getRoomId() : roomId,
                    session,
                    e.getErrorCode(),
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error("Unexpected error handling message: userId={}, roomId={}", user != null ? user.guid() : null, roomId, e);
            sendError(user, roomId, session, ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected abstract void handleMessage(WebSocketSession session, TextMessage message) throws Exception;

    protected abstract void onJoin(UUID roomId, UserInternalResponse user);

    protected abstract void onLeave(UUID roomId, UserInternalResponse user);

    protected void sendError(UserInternalResponse user, UUID roomId, WebSocketSession session, ErrorCode errorCode, String debugMessage) {
        if (user == null || user.guid() == null) {
            log.warn("Cannot send error — userId is null. errorCode={}, session={}", errorCode, session.getId());
            return;
        }

        ClientSession client = sessionManager.getByGuid(user.guid());

        if (client == null) {
            log.warn("Cannot send error — client not found. userId={}, errorCode={}", user.guid(), errorCode);
            return;
        }

        log.info("Error detail for userId={}, errorCode={}: {}", user.guid(), errorCode, debugMessage);

        ErrorMessage errorMessage = ErrorMessage.builder()
                .type(MessageType.SYSTEM)
                .event(ErrorEvent.ERROR)
                .toUserId(user.guid())
                .roomId(roomId)
                .message(errorCode.getMessage())
                .build();

        sessionManager.sendToSession(client, errorMessage);

        log.info("Error sent: userId={}, roomId={}, errorCode={}", user.guid(), roomId, errorCode);
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
