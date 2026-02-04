package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomEvent;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Deprecated
@Service
@Slf4j
public class RoomManager extends AbstractRoomManager {

    private final MessageMapper messageMapper;

    public RoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> factory,
            SessionManager sessionManager,
            RoomValidator validator,
            @Qualifier("messageMapperImpl") MessageMapper mapper
    ) {
        super(serializer, factory, sessionManager, validator);
        this.messageMapper = mapper;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.ROOM_TEST;
    }

    @Override
    public MessageMapper getMapper() {
        return this.messageMapper;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                RoomEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                user.username() + " joined room: " + room.getName()
        ));
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                RoomEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                user.username() + " left room: " + room.getName()
        ));
    }
}
