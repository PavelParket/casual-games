package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.Room;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RoomFactory implements ObjectFactory<Room> {

    @Override
    public Room create(Object... objects) {
        if (objects.length < 1 || !(objects[0] instanceof String name)) {
            throw new IllegalArgumentException("Expected room name as a String");
        }

        return create(name);
    }

    public Room create(String name) {
        return Room.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .build();
    }
}
