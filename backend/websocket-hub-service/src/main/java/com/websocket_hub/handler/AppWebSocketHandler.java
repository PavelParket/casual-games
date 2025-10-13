package com.websocket_hub.handler;

import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.SessionManager;
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
        String userId = getUserIdFromSession(session);
        String roomId = getRoomIdFromSession(session);

        sessionManager.register(userId, session);
        roomManager.addSession(roomId, session);

        onJoin(roomId, userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        String roomId = getRoomIdFromSession(session);

        sessionManager.remove(userId);
        roomManager.removeSession(roomId, session);

        onLeave(roomId, userId, session);
    }

    protected abstract void onJoin(String roomId, String userId, WebSocketSession session);

    protected abstract void onLeave(String roomId, String userId, WebSocketSession session);

    protected String getUserIdFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("userId");
    }

    protected String getRoomIdFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("roomId");
    }
}
