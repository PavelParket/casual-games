package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.message.HorseRaceMessage;
import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.mapper.HorseRaceGameMessageMapper;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HorseRaceGameRoomManager extends AbstractRoomManager {

    private final Map<UUID, Set<UUID>> readyPlayers = new ConcurrentHashMap<>();

    private final MessageMapper messageMapper;

    public HorseRaceGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            SessionManager sessionManager,
            RoomValidator validator,
            RoomRedisRepository redisRepository,
            HorseRaceGameMessageMapper horseRaceGameMessageMapper
    ) {
        super(serializer, roomFactory, sessionManager, validator, redisRepository);
        this.messageMapper = horseRaceGameMessageMapper;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.HORSE_RACE;
    }

    @Override
    public MessageMapper getMapper() {
        return messageMapper;
    }

    @Override
    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.HORSE_RACE_ROOM;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} joined horse race room {}", user.email(), user.username(), room.getName());

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} left horse race room {}", user.email(), user.username(), room.getName());

        readyPlayers.computeIfPresent(room.getId(), (key, players) -> {
            players.remove(user.guid());
            return players.isEmpty() ? null : players;
        });

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has left the room " + room.getName()
        ));
    }

    @Override
    protected void onCreateRoom(Room room) {

    }

    @Override
    protected void onDeleteRoom(UUID roomId) {

    }

    @Override
    public Integer getReadyPlayerCount(UUID roomId) {
        return readyPlayers.getOrDefault(roomId, Set.of()).size();
    }

    public void savePreset(UUID roomId, HorseRaceMessage preset) {
        roomPresets.put(roomId, preset);
        log.info("Preset saved for room={}: horseCount={}", roomId, preset.horseCount());
    }

    public HorseRaceMessage getPreset(UUID roomId) {
        return roomPresets.get(roomId);
    }

    public void removePreset(UUID roomId) {
        roomPresets.remove(roomId);
    }

    public void markReady(UUID roomId, UserInternalResponse user) {
        Room room = getRoomsMap().getOrDefault(roomId, null);

        if (room == null) {
            throw new IllegalArgumentException("Room id=" + roomId + " not found");
        }

        Set<UUID> ready = readyPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        ready.add(user.guid());

        log.info("Player {} is ready in room {}. Total ready: {}", user.username(), roomId, ready.size());

        broadcast(roomId, messageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.READY,
                user.guid(),
                null,
                roomId,
                "Player " + user.username() + " is ready"
        ));
    }

    public boolean areAllPlayersReady(UUID roomId) {
        Set<UUID> ready = readyPlayers.get(roomId);
        Set<ClientSession> players = getPlayersInRoom(roomId);

        return ready != null
                && !players.isEmpty()
                && ready.size() == players.size()
                && ready.containsAll(players.stream().map(ClientSession::getGuid).collect(Collectors.toSet()));
    }

    public Map<UUID, String> getParticipants(UUID roomId) {
        return getPlayersInRoom(roomId).stream()
                .collect(Collectors.toMap(ClientSession::getGuid, ClientSession::getUsername));
    }

    public void removeReadyPlayers(UUID roomId) {
        readyPlayers.remove(roomId);
        log.info("Cleared ready players for room={}", roomId);
    }
}
