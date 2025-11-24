package com.websocket_hub.service;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomManagerService {
    public void leave(String roomName, RoomType roomType, Map<String, Room> rooms, ClientSession client) {
        Room room = rooms.get(roomName);

        if (room == null) {
            throw new RuntimeException("The room " + roomName + " does not exist!");
        }

        if (!room.getType().equals(roomType)) {
            throw new RuntimeException("There is no room with type " + roomType);
        }

        synchronized (room) {
            room.remove(client);
        }

        delete(room, rooms);
    }

    public void delete(Room room, Map<String, Room> rooms) {
        if (room.isEmpty()) {
            log.info("Room \"{}\" is now empty, removing...", room.getName());

            rooms.remove(room.getName());
        }
    }

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
