package com.websocket_hub.handler;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
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
        UUID guid = WebSocketUtil.getGuid(session);
        UserInfoInternalResponse user = WebSocketUtil.getUser(session);
        String roomName = WebSocketUtil.getRoomName(session);
        Instant connectedAt = WebSocketUtil.getConnectedAt(session);

        sessionManager.register(guid, user, session, connectedAt);
        roomManager.addSession(roomName, user, session);

        onJoin(roomName, user);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        UUID guid = WebSocketUtil.getGuid(session);
        UserInfoInternalResponse user = WebSocketUtil.getUser(session);
        String roomName = WebSocketUtil.getRoomName(session);

        roomManager.removeSession(roomName, user, session);
        sessionManager.remove(guid);

        onLeave(roomName, user);
    }

    protected abstract void onJoin(String roomName, UserInfoInternalResponse user);

    protected abstract void onLeave(String roomName, UserInfoInternalResponse user);
}
