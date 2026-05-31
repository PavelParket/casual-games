package com.websocket_hub.service;

import com.common_utils.exception.NotFoundException;
import com.websocket_hub.domain.dto.request.RoomFilterRequest;
import com.websocket_hub.domain.dto.request.RoomRequest;
import com.websocket_hub.domain.dto.response.PlayerResponse;
import com.websocket_hub.domain.dto.response.RoomResponse;
import com.websocket_hub.domain.dto.response.RoomResponseMap;
import com.websocket_hub.domain.dto.response.RoomStatusResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomSortField;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.SortDirection;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.mapper.PlayerMapper;
import com.websocket_hub.mapper.RoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.websocket_hub.config.ResourceMessageConstants.ROOM_NOT_FOUND;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_TYPE_DOES_NOT_EXIST;

@Service
@Slf4j
public class RoomService {

    private final Map<RoomType, AbstractRoomManager> roomManagers;

    private final RoomMapper roomMapper;

    private final PlayerMapper playerMapper;

    public RoomService(
            List<AbstractRoomManager> managers,
            RoomMapper roomMapper,
            PlayerMapper playerMapper
    ) {
        this.roomManagers = Arrays.stream(RoomType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> managers.stream()
                                .filter(manager -> type.equals(manager.getRoomType()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No manager found for room type: " + type))
                ));
        this.roomMapper = roomMapper;
        this.playerMapper = playerMapper;
    }

    public Map<UUID, PlayerResponse> getPlayers(UUID roomId, RoomType roomType) {
        return getManager(roomType).getPlayersInRoom(roomId).stream()
                .collect(Collectors.toMap(
                        ClientSession::getGuid,
                        playerMapper::toResponse
                ));
    }

    public Integer getReadyPlayerCount(UUID roomId, RoomType roomType) {
        return getManager(roomType).getReadyPlayerCount(roomId);
    }

    public List<RoomType> getTypes() {
        return List.of(RoomType.values());
    }

    public RoomResponse create(RoomRequest roomRequest) {
        return roomMapper.toResponse(getManager(roomRequest.roomType()).create(roomRequest));
    }

    public RoomResponse getById(UUID id) {
        return roomMapper.toResponse(roomManagers.values().stream()
                .filter(manager -> manager.getRedisKey() != null)
                .flatMap(manager -> manager.getRoomsList().stream())
                .filter(room -> room.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND))
        );
    }

    private AbstractRoomManager getManager(RoomType roomType) {
        AbstractRoomManager roomManager = roomManagers.get(roomType);

        if (roomManager == null) {
            throw new NotFoundException(ROOM_TYPE_DOES_NOT_EXIST);
        }

        return roomManager;
    }

    public RoomStatusResponse getStatus(UUID roomId, RoomType roomType) {
        return RoomStatusResponse.builder()
                .roomStatus(getManager(roomType).getStatus(roomId))
                .build();
    }

    public RoomResponseMap search(RoomFilterRequest request) {
        Set<RoomType> types = (request.types() == null || request.types().isEmpty())
                ? Set.of(RoomType.values())
                : request.types();

        Comparator<Room> comparator = getComparator(request.sortField(), request.sortDirection());

        Map<RoomType, List<RoomResponse>> rooms = Arrays.stream(RoomType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> types.contains(type)
                                ? getManager(type).getRoomsList().stream()
                                .filter(this::isJoinable)
                                .filter(room -> room.size() < type.getMaxParticipants())
                                .filter(room -> matchesName(room, request.name()))
                                .sorted(comparator)
                                .map(roomMapper::toResponse)
                                .toList()
                                : List.of(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return new RoomResponseMap(rooms);
    }

    private boolean isJoinable(Room room) {
        if (RoomType.DE_CODER.equals(room.getType())) {
            return !RoomStatus.FINISHED.equals(room.getStatus());
        }

        return RoomStatus.WAITING.equals(room.getStatus()) || RoomStatus.PENDING_DELETE.equals(room.getStatus());
    }

    private boolean matchesName(Room room, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }

        return room.getName().toLowerCase().contains(name.toLowerCase());
    }

    private Comparator<Room> getComparator(RoomSortField field, SortDirection direction) {
        Comparator<Room> comparator = switch (field) {
            case NAME -> Comparator.comparing(Room::getName, String.CASE_INSENSITIVE_ORDER);
            case CREATED_AT -> Comparator.comparing(Room::getCreatedAt);
        };

        return direction == SortDirection.DESC ? comparator.reversed() : comparator;
    }
}
