package com.websocket_hub.handler;

import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@AllArgsConstructor
@Slf4j
public abstract class AppWebSocketHandler<T extends AbstractRoomManager> extends TextWebSocketHandler {

    protected final SessionManager sessionManager;

    protected final T roomManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = WebSocketUtil.getUserId(session);
        String username = WebSocketUtil.getUsername(session);
        String roomName = WebSocketUtil.getRoomName(session);

        sessionManager.register(userId, username, session);
        roomManager.addSession(roomName, userId, username, session);

        onJoin(roomName, username);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = WebSocketUtil.getUserId(session);
        String username = WebSocketUtil.getUsername(session);
        String roomName = WebSocketUtil.getRoomName(session);

        sessionManager.remove(userId);
        roomManager.removeSession(roomName, username, session);

        onLeave(roomName, username);
    }

    protected abstract void onJoin(String roomName, String username);

    protected abstract void onLeave(String roomName, String username);
}
