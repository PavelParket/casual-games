package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.EventType;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRoomManager {

    private final MessageSerializer serializer;

    private final RoomFactory roomFactory;

    private final SessionManager sessionManager;

    private final RoomValidator validator;

    private final RoomRedisRepository redisRepository;

    public abstract RoomType getRoomType();

    public abstract MessageMapper getMapper();

    public abstract RoomTypeRedisKey getRedisKey();

    protected abstract void onAddSession(UserInternalResponse user, Room room, WebSocketSession session);

    protected abstract void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session);

    protected abstract void onCreateRoom(Room room);

    protected abstract void onDeleteRoom(UUID roomId);

    protected boolean allowsLateJoin() {
        return false;
    }

    public void addSession(UUID roomId, UserInternalResponse user, WebSocketSession session) {
        RoomMetadata metadata = redisRepository.get(roomId, getRedisKey());

        if (metadata != null) {
            RoomStatus status = metadata.getStatus() != null ? metadata.getStatus() : RoomStatus.WAITING;

            switch (status) {
                case FINISHED -> {
                    log.warn("Rejected join — room is FINISHED: roomId={}, user={}", roomId, user.username());
                    return;
                }
                case IN_PROGRESS -> {
                    if (!allowsLateJoin()) {
                        log.warn("Rejected join — game IN_PROGRESS, late join not allowed: roomId={}, user={}", roomId, user.username());
                        return;
                    }
                }
                case PENDING_DELETE -> {
                    log.info("Room id={} was PENDING_DELETE — rolling back to WAITING (user={} joined)", roomId, user.username());
                    metadata.setStatus(RoomStatus.WAITING);
                    redisRepository.save(metadata, getRedisKey());
                }
                default -> {
                }
            }
        }

        Room room = metadata != null ? restoreRoom(metadata) : restoreRoom(roomId);
        ClientSession client = sessionManager.getByGuid(user.guid());

        if (room == null) {
            log.warn("Cannot add session — room not found: roomId={}", roomId);
            return;
        }

        if (client == null || !client.validateSession(session)) {
            return;
        }

        room.add(client);

        redisRepository.addParticipantAndUpdateCount(roomId, user.guid(), getRedisKey());

        onAddSession(user, room, session);

        log.info("Session \"{}\" [user \"{}\"] joined room \"{}\"", session.getId(), user.email(), room.getName());
    }

    public void removeSession(UUID roomId, UserInternalResponse user, WebSocketSession session) {
        Room room = restoreRoom(roomId);
        ClientSession client = sessionManager.getByGuid(user.guid());

        if (room == null) {
            log.warn("Cannot remove session — room not found: roomId={}", roomId);
            return;
        }

        if (client == null || !client.validateSession(session)) {
            return;
        }

        room.remove(client);

        redisRepository.removeParticipantAndUpdateCount(roomId, user.guid(), getRedisKey());

        onRemoveSession(user, room, session);

        log.info("Session \"{}\" [user \"{}\"] left room \"{}\"", session.getId(), user.email(), room.getName());
    }

    public Room create(RoomRequest roomRequest) {
        Set<RoomMetadata> metadata = redisRepository.getAll(getRedisKey());

        synchronized (metadata) {
            if (validator.isRoomNameExists(roomRequest, metadata)) {
                throw new RuntimeException("Room with name: " + roomRequest.roomName() + " already exists!");
            }

            Room room = roomFactory.create(roomRequest.roomName(), roomRequest.roomType());

            redisRepository.save(RoomMetadata.create(room), getRedisKey());

            onCreateRoom(room);

            log.info("Room name={} id={} was created", room.getName(), room.getId());

            return room;
        }
    }

    public void delete(UUID roomId) {
        if (!redisRepository.roomExists(roomId, getRedisKey())) {
            log.warn("Room id={} not found", roomId);
            return;
        }

        redisRepository.deleteFullRoom(roomId, getRedisKey());

        onDeleteRoom(roomId);

        log.info("Room id={} was deleted", roomId);
    }

    public void broadcast(UUID roomId, Message<? extends EventType> message) {
        try {
            String json = serializer.serialize(message);

            Room room = restoreRoom(roomId);

            if (room == null || room.isEmpty()) {
                return;
            }

            Set<ClientSession> dead = ConcurrentHashMap.newKeySet();

            List<Thread> threads = new ArrayList<>();

            for (ClientSession clientSession : room.getParticipants()) {
                if (clientSession == null || !clientSession.isOpen()) {
                    dead.add(clientSession);

                    continue;
                }

                Thread thread = Thread.ofVirtual().start(() -> {
                    try {
                        clientSession.sendMessage(new TextMessage(json));
                        log.info("Sent message: {}", json);
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}: {}", clientSession.getSession().getId(), e.getMessage());

                        dead.add(clientSession);
                    }
                });

                threads.add(thread);
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Broadcast interrupted for room {}", roomId);
                }
            }

            if (!dead.isEmpty()) {
                dead.forEach(clientSession ->
                        redisRepository.removeParticipantAndUpdateCount(
                                room.getId(),
                                clientSession.getGuid(),
                                getRedisKey()
                        )
                );
            }

            log.info("Broadcast in room \"{}\" from {} → {} recipients", roomId, message.fromUserId(), room.size());
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
        }
    }

    protected void sendToClient(ClientSession client, Message<? extends EventType> message) {
        if (client == null) {
            log.warn("Cannot send message — client is null");
            return;
        }

        sessionManager.sendToSession(client, message);
    }

    public Map<UUID, Room> getRoomsMap() {
        Map<UUID, Set<UUID>> participants = redisRepository.getParticipantsByRoom();

        return roomFactory.createSetFromMetadata(
                        redisRepository.getAll(getRedisKey()),
                        participants,
                        sessionManager.getAll()
                ).stream()
                .collect(Collectors.toMap(
                        Room::getId,
                        Function.identity()
                ));
    }

    public List<Room> getRoomsList() {
        Map<UUID, Set<UUID>> participants = redisRepository.getParticipantsByRoom();

        return roomFactory.createSetFromMetadata(
                        redisRepository.getAll(getRedisKey()),
                        participants,
                        sessionManager.getAll()
                ).stream()
                .toList();
    }

    public Set<ClientSession> getPlayersInRoom(UUID roomId) {
        return redisRepository.getParticipants(roomId).stream()
                .map(sessionManager::getByGuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Integer getReadyPlayerCount(UUID roomId) {
        return 0;
    }

    protected ClientSession getClientSessionByGuid(UUID guid) {
        return sessionManager.getByGuid(guid);
    }

    private Room restoreRoom(UUID roomId) {
        RoomMetadata metadata = redisRepository.get(roomId, getRedisKey());

        if (metadata == null) {
            return null;
        }

        return restoreRoom(metadata);
    }

    private Room restoreRoom(RoomMetadata metadata) {
        Set<UUID> participants = redisRepository.getParticipants(metadata.getId());
        return roomFactory.createFromMetadata(metadata, participants, sessionManager.getAll());
    }

    public void updateRoomStatus(UUID roomId, RoomStatus status) {
        RoomMetadata metadata = redisRepository.get(roomId, getRedisKey());

        if (metadata == null) {
            log.warn("Cannot update status — room not found: roomId={}", roomId);
            return;
        }

        metadata.setStatus(status);

        if (status == RoomStatus.FINISHED) {
            metadata.setGameFinishedAt(Instant.now());
        }

        redisRepository.save(metadata, getRedisKey());

        log.info("Room id={} status updated to {}", roomId, status);
    }

    public Set<RoomMetadata> getAllMetadata() {
        return redisRepository.getAll(getRedisKey());
    }

    public void kickAll(UUID roomId) {
        getPlayersInRoom(roomId).forEach(client -> {
            sessionManager.remove(client.getGuid());
        });
    }
}
