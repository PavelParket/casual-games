package com.websocket_hub.handler;

import com.websocket_hub.enums.SystemEvents;
import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class RoomHandler extends AppWebSocketHandler<RoomManager> {

    private final MessageMapper mapper;

    public RoomHandler(SessionManager sessionManager, RoomManager roomManager, MessageMapper mapper) {
        super(sessionManager, roomManager);
        this.mapper = mapper;
    }

    @Override
    protected void onJoin(String roomId, String userId, WebSocketSession session) {
        roomManager.broadcast(roomId, mapper.toResponse(roomId, userId + " " + SystemEvents.JOIN.getDescription() + " room: [" + roomId + "]"));
    }

    @Override
    protected void onLeave(String roomId, String userId, WebSocketSession session) {
        roomManager.broadcast(roomId, mapper.toResponse(roomId, userId + " " + SystemEvents.LEFT.getDescription() + "room: [" + roomId + "]"));
    }
}
