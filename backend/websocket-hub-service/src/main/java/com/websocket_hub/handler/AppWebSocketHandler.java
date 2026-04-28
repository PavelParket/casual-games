package com.websocket_hub.handler;

import com.websocket_hub.domain.context.WebSocketContext;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
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

    protected WebSocketErrorHandler errorHandler;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        UserInternalResponse user = null;
        UUID roomId = null;
        Instant connectedAt = null;

        try {
            user = WebSocketUtil.getUser(session);
            roomId = WebSocketUtil.getRoomId(session);
            connectedAt = WebSocketUtil.getConnectedAt(session);

            sessionManager.register(user.guid(), user, session, connectedAt);
            roomManager.addSession(roomId, user, session);

            onJoin(roomId, user);

            log.info("Connection established: userId={}, roomId={}", user.guid(), roomId);

        } catch (Exception e) {
            errorHandler.handle(WebSocketContext.of(user, roomId, session, connectedAt), e, true);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        try {
            UserInternalResponse user = WebSocketUtil.getUser(session);
            UUID roomId = WebSocketUtil.getRoomId(session);

            roomManager.removeSession(roomId, user, session);
            sessionManager.removeIfCurrent(user.guid(), session);

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
        Instant connectedAt = null;

        try {
            user = WebSocketUtil.getUser(session);
            roomId = WebSocketUtil.getRoomId(session);
            connectedAt = WebSocketUtil.getConnectedAt(session);

            handleMessage(session, message);

        } catch (Exception e) {
            errorHandler.handle(WebSocketContext.of(user, roomId, session, connectedAt), e, false);
        }
    }

    protected abstract void handleMessage(WebSocketSession session, TextMessage message) throws Exception;

    protected abstract void onJoin(UUID roomId, UserInternalResponse user);

    protected abstract void onLeave(UUID roomId, UserInternalResponse user);
}
