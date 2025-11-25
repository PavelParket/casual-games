package com.websocket_hub.validator;

import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class RoomValidator {
    public void validateRoom(Room room) {
        if (room == null) {
            throw new RuntimeException("Room is missing!");
        }
    }

    public void validateRoomType(Room room, RoomType roomType) {
        if (!room.getType().equals(roomType)) {
            throw new RuntimeException("There is no room with type " + roomType);
        }
    }

    public boolean isRoomExists(String roomName, Map<String, Room> rooms) {
        return rooms.containsKey(roomName);
    }
}
