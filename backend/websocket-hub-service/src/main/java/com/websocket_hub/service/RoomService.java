package com.websocket_hub.service;

import com.websocket_hub.domain.dto.RoomInfoResponse;
import com.websocket_hub.domain.dto.RoomTypeResponse;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.mapper.RoomMapper;
import com.websocket_hub.mapper.RoomTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final Map<RoomType, AbstractRoomManager> managers;

    private final RoomMapper mapper;

    private final RoomTypeMapper roomTypeMapper;

    public RoomService(List<AbstractRoomManager> managers, RoomMapper mapper, RoomTypeMapper roomTypeMapper) {
        this.managers = Arrays.stream(RoomType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> managers.stream()
                                .filter(manager -> type.getManagerClass().isAssignableFrom(manager.getClass()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + type))
                ));
        this.mapper = mapper;
        this.roomTypeMapper = roomTypeMapper;
    }

    public List<RoomInfoResponse> getRooms() {
        return managers.values().stream()
                .flatMap(manager -> manager.getActiveRooms().stream())
                .map(mapper::toResponse)
                .toList();
    }

    public Map<String, Room> getRoomsByType(RoomType roomType) {
        return getManager(roomType)
                .map(AbstractRoomManager::getRooms)
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType));
    }

    public List<String> getPlayerEmailsInRoom(String roomName, RoomType roomType) {
        return getManager(roomType)
                .map(manager -> manager.getUserEmails(roomName).stream().toList())
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType));
    }

    public Integer getReadyPlayerCount(String roomName, RoomType roomType) {
        return getManager(roomType)
                .map(manager -> manager.getReadyPlayerCount(roomName))
                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + roomType));
    }

    public List<RoomTypeResponse> getTypes() {
        return Arrays.stream(RoomType.values())
                .map(roomTypeMapper::toResponse)
                .toList();
    }

    private Optional<AbstractRoomManager> getManager(RoomType roomType) {
        return Optional.ofNullable(managers.get(roomType));
    }
}
