package com.websocket_hub.validator;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.entity.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class RoomValidator {

    public boolean isRoomExists(RoomRequest roomRequest, Map<UUID, Room> rooms) {
        return rooms.values().stream()
                .anyMatch(room -> room.getName().equals(roomRequest.roomName()));
    }
}
