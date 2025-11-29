package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomFactory implements ObjectFactory<Room> {

    @Override
    public Room create(Object... objects) {
        if (objects.length != 2
                || !(objects[0] instanceof String roomName)
                || !(objects[1] instanceof RoomType roomType)
        ) {
            throw new IllegalArgumentException("Expected room name as a String");
        }

        return create(roomName, roomType);
    }

    private Room create(String roomName, RoomType roomType) {
        return Room.builder()
                .id(UUID.randomUUID())
                .name(roomName)
                .type(roomType)
                .participants(ConcurrentHashMap.newKeySet())
                .createdAt(Instant.now())
                .build();
    }
}
