package com.websocket_hub.validator;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.entity.RoomMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class RoomValidator {

    public boolean isRoomExists(RoomRequest roomRequest, Map<UUID, Room> rooms) {
        return rooms.values().stream()
                .anyMatch(room -> room.getName().equals(roomRequest.roomName()));
    }

    public boolean isRoomNameExists(RoomRequest roomRequest, Set<RoomMetadata> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return false;
        }

        return metadata.stream()
                .anyMatch(roomMetadata -> roomMetadata.getName().equals(roomRequest.roomName()));
    }
}
