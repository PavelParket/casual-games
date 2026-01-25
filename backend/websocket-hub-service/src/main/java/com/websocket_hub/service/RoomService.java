package com.websocket_hub.service;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.mapper.RoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final Map<RoomType, AbstractRoomManager> managers;

    private final RoomMapper roomMapper;

    public RoomService(List<AbstractRoomManager> managers, RoomMapper roomMapper) {
        this.managers = Arrays.stream(RoomType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> managers.stream()
                                .filter(manager -> type.equals(manager.getRoomType()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + type))
                ));
        this.roomMapper = roomMapper;

        log.warn("Map of managers: {}", managers);
    }

    public List<RoomResponse> getRooms() {
        return managers.values().stream()
                .flatMap(manager -> manager.getRoomsList().stream())
                .map(roomMapper::toResponse)
                .toList();
    }

    public List<RoomResponse> getRoomsByType(RoomType roomType) {
        return getManager(roomType)
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType))
                .getRoomsList().stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    public Map<UUID, String> getUsernamesInRoom(UUID roomId, RoomType roomType) {
        return getManager(roomType)
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType))
                .getUsersInRoom(roomId).stream()
                .collect(Collectors.toMap(
                        ClientSession::getGuid,
                        ClientSession::getUsername
                ));
    }

    public Integer getReadyPlayerCount(UUID roomId, RoomType roomType) {
        return getManager(roomType)
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType))
                .getReadyPlayerCount(roomId);
    }

    public List<RoomType> getTypes() {
        return List.of(RoomType.values());
    }

    private Optional<AbstractRoomManager> getManager(RoomType roomType) {
        return Optional.ofNullable(managers.get(roomType));
    }

    public RoomResponse create(RoomRequest roomRequest) {
        return roomMapper.toResponse(getManager(roomRequest.roomType())
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomRequest.roomType()))
                .create(roomRequest));
    }
}
