package com.websocket_hub.manager;

import com.websocket_hub.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@Slf4j
public class RoomManager extends AbstractRoomManager {

    public RoomManager(MessageSerializer<String> serializer) {
        super(serializer);
    }

    @Override
    public String getName() {
        return "roomManager";
    }

    @Override
    protected void onAddSession(String roomId, WebSocketSession session) {

    }

    @Override
    protected void onRemoveSession(String roomId, WebSocketSession session) {

    }
}
