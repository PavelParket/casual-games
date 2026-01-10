package com.websocket_hub.validator;

import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class RoomValidator {
    public boolean validateRoomType(Room room, RoomType roomType) {
        return room.getType().equals(roomType);
    }

    public boolean isRoomExists(String roomName, Map<String, Room> rooms) {
        return rooms.containsKey(roomName);
    }
}
