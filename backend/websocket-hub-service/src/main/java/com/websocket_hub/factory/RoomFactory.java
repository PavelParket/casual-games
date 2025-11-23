package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.Room;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoomFactory implements ObjectFactory<Room> {

    @Override
    public Room create(Object... objects) {
        if (objects.length < 1 || !(objects[0] instanceof String name)) {
            throw new IllegalArgumentException("Expected room name as a String");
        }

        return create(name);
    }

    private Room create(String name) {
        return Room.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }
}
