package com.websocket_hub.manager;

import com.websocket_hub.entity.ClientSession;
import com.websocket_hub.entity.Room;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@Slf4j
public class RoomManager extends AbstractRoomManager {

    public RoomManager(MessageSerializer<String> serializer, ObjectFactory<Room> roomFactory, ObjectFactory<ClientSession> clientFactory) {
        super(serializer, roomFactory, clientFactory);
    }

    @Override
    public String getName() {
        return "roomManager";
    }

    @Override
    protected void onAddSession(String roomName, WebSocketSession session) {

    }

    @Override
    protected void onRemoveSession(String roomName, WebSocketSession session) {

    }
}
