package com.websocket_hub.handler;

import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoomHandler extends AppWebSocketHandler<RoomManager> {

    public RoomHandler(SessionManager sessionManager, RoomManager roomManager) {
        super(sessionManager, roomManager);
    }

    @Override
    protected void onJoin(String roomId, String username) {
    }

    @Override
    protected void onLeave(String roomId, String username) {
    }
}
