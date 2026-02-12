package com.websocket_hub.factory;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public Room createFromMetadata(RoomMetadata roomMetadata,
                                   Set<UUID> participantsFromRedis,
                                   Map<UUID, ClientSession> participantsFromSessions) {
        Set<ClientSession> participants = participantsFromRedis.stream()
                .map(participantsFromSessions::get)
                .collect(Collectors.toSet());

        return Room.builder()
                .id(roomMetadata.getId())
                .name(roomMetadata.getName())
                .type(roomMetadata.getType())
                .participants(participants)
                .createdAt(roomMetadata.getCreatedAt())
                .build();
    }

    public Set<Room> createSetFromMetadata(Set<RoomMetadata> roomMetadata,
                                           Map<UUID, Set<UUID>> participantsFromRedis,
                                           Map<UUID, ClientSession> participantsFromSessions) {
        return roomMetadata.stream()
                .map(metadata ->
                        createFromMetadata(metadata, participantsFromRedis.get(metadata.getId()), participantsFromSessions)
                )
                .collect(Collectors.toSet());
    }
}
