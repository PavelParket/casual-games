package com.websocket_hub.handler;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
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
        UserInternalResponse user = WebSocketUtil.getUser(session);
        UUID roomId = WebSocketUtil.getRoomId(session);
        Instant connectedAt = WebSocketUtil.getConnectedAt(session);

        sessionManager.register(user.guid(), user, session, connectedAt);

        roomManager.addSession(roomId, user, session);

        onJoin(roomId, user);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        UserInternalResponse user = WebSocketUtil.getUser(session);
        UUID roomId = WebSocketUtil.getRoomId(session);

        roomManager.removeSession(roomId, user, session);

        sessionManager.remove(user.guid());

        onLeave(roomId, user);
    }

    protected abstract void onJoin(UUID roomId, UserInternalResponse user);

    protected abstract void onLeave(UUID roomId, UserInternalResponse user);
}
