package com.websocket_hub.manager;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.enums.MessageType;
import com.websocket_hub.enums.SystemEvents;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@Slf4j
public class RoomManager extends AbstractRoomManager {

    private final MessageMapper mapper;

    public RoomManager(MessageSerializer<String> serializer, ObjectFactory<Room> roomFactory, ObjectFactory<ClientSession> clientFactory, MessageMapper mapper) {
        super(serializer, roomFactory, clientFactory);
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "roomManager";
    }

    @Override
    protected void onAddSession(String username, String roomName, WebSocketSession session) {
        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, roomName, username + " " + SystemEvents.JOIN.getDescription() + " room: [" + roomName + "]"));
    }

    @Override
    protected void onRemoveSession(String username, String roomName, WebSocketSession session) {
        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, roomName, username + " " + SystemEvents.LEFT.getDescription() + "room: [" + roomName + "]"));
    }
}
