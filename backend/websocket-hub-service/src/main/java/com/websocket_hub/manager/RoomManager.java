package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.SystemEvent;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.service.RoomManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@Slf4j
public class RoomManager extends AbstractRoomManager {

    private final MessageMapper mapper;

    public RoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> roomFactory,
            SessionManager sessionManager,
            RoomManagerService service,
            @Qualifier("messageMapperImpl") MessageMapper mapper
    ) {
        super(serializer, roomFactory, sessionManager, service);
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "RoomManager";
    }

    @Override
    protected void onAddSession(UserInfoInternalResponse user, String roomName, WebSocketSession session) {
        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.JOIN, roomName, user.username() + " " + SystemEvent.JOIN.getDescription() + " room: " + roomName));
    }

    @Override
    protected void onRemoveSession(UserInfoInternalResponse user, String roomName, WebSocketSession session) {
        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.LEAVE, roomName, user.username() + " " + SystemEvent.LEAVE.getDescription() + "room: " + roomName));
    }

    @Override
    protected boolean validateManagerType(RoomType roomType) {
        return this.getClass().equals(roomType.getManagerClass());
    }
}
