package com.websocket_hub.handler;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.enums.RoomType;
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
        RoomType roomType = WebSocketUtil.getRoomType(session);
        Instant connectedAt = WebSocketUtil.getConnectedAt(session);
        String action = WebSocketUtil.getAction(session);

        sessionManager.register(guid, user, session, connectedAt);

        if ("create".equals(action)) {
            roomManager.create(roomName, roomType);
            roomManager.addSession(roomName, roomType, user, session);
        } else if ("join".equals(action)) {
            roomManager.addSession(roomName, roomType, user, session);
        } else {
            log.warn("Unknown action '{}' for user {} in room {}", action, user.email(), roomName);
            session.close(CloseStatus.BAD_DATA);
            sessionManager.remove(guid);
            return;
        }

        onJoin(roomName, user);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        UUID guid = WebSocketUtil.getGuid(session);
        UserInfoInternalResponse user = WebSocketUtil.getUser(session);
        String roomName = WebSocketUtil.getRoomName(session);
        RoomType roomType = WebSocketUtil.getRoomType(session);

        roomManager.removeSession(roomName, roomType, user, session);

        sessionManager.remove(guid);

        roomManager.delete(roomName, roomType);

        onLeave(roomName, user);
    }

    protected abstract void onJoin(String roomName, UserInfoInternalResponse user);

    protected abstract void onLeave(String roomName, UserInfoInternalResponse user);
}
