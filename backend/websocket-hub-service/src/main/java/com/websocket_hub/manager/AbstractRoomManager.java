package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.RoomRequest;
import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.EventType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.RoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRoomManager {

    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>();

    private final MessageSerializer<String> serializer;

    private final ObjectFactory<Room> factory;

    private final SessionManager sessionManager;

    private final RoomValidator validator;

    public abstract RoomType getRoomType();

    public abstract MessageMapper getMapper();

    protected abstract void onAddSession(UserInternalResponse user, Room room, WebSocketSession session);

    protected abstract void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session);

    public void addSession(UUID roomId, UserInternalResponse user, WebSocketSession session) {
        Room room = rooms.get(roomId);
        ClientSession client = sessionManager.getByGuid(user.guid());

        if (room != null && client != null && client.validateSession(session)) {
            synchronized (room) {
                room.add(client);
            }
        } else {
            return;
        }

        onAddSession(user, room, session);

        log.info("Session \"{}\" [user \"{}\"] joined room \"{}\"", session.getId(), user.email(), room.getName());
    }

    public void removeSession(UUID roomId, UserInternalResponse user, WebSocketSession session) {
        Room room = rooms.get(roomId);
        ClientSession client = sessionManager.getByGuid(user.guid());

        if (room != null && client != null && client.validateSession(session)) {
            synchronized (room) {
                room.remove(client);
            }
        } else {
            return;
        }

        onRemoveSession(user, room, session);

        log.info("Session \"{}\" [user \"{}\"] left room \"{}\"", session.getId(), user.email(), room.getName());
    }

    public Room create(RoomRequest roomRequest) {
        synchronized (rooms) {
            if (validator.isRoomExists(roomRequest, rooms)) {
                throw new RuntimeException("Room with name: " + roomRequest.roomName() + " already exists!");
            }

            Room room = factory.create(roomRequest.roomName(), roomRequest.roomType());
            rooms.put(room.getId(), room);

            log.info("Room name={} id={} was created", room.getName(), room.getId());

            return room;
        }
    }

    public void delete(UUID roomId) {
        synchronized (rooms) {
            Room room = rooms.getOrDefault(roomId, null);

            if (room == null) {
                log.warn("Room id={} not found", roomId);

                return;
            }

            rooms.remove(roomId);

            log.info("Room name={} id={} was deleted", room.getName(), room.getId());
        }
    }

    public void broadcast(UUID roomId, Message<? extends EventType> message) {
        try {
            String json = serializer.serialize(message);

            Room room = rooms.get(roomId);

            if (room == null || room.isEmpty()) {
                return;
            }

            Set<ClientSession> dead = ConcurrentHashMap.newKeySet();

            for (ClientSession clientSession : room.getParticipants()) {
                if (clientSession == null || !clientSession.isOpen()) {
                    dead.add(clientSession);

                    continue;
                }

                Thread.ofVirtual().start(() -> {
                    try {
                        clientSession.sendMessage(new TextMessage(json));
                        log.info("Sent message: {}", json);
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}: {}", clientSession.getSession().getId(), e.getMessage());

                        dead.add(clientSession);
                    }
                });
            }

            if (!dead.isEmpty()) {
                synchronized (room) {
                    room.getParticipants().removeAll(dead);

                    if (room.isEmpty()) {
                        rooms.remove(roomId);

                        log.info("Room \"{}\" removed due to all sessions being closed", roomId);
                    }
                }
            }

            log.info("Broadcast in room \"{}\" from {} → {} recipients", roomId, message.fromUserId(), room.size());
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
        }
    }

    public Map<UUID, Room> getRoomsMap() {
        return Map.copyOf(rooms);
    }

    public List<Room> getRoomsList() {
        return rooms.values().stream().toList();
    }

    public Set<ClientSession> getUsersInRoom(UUID roomId) {
        Room room = rooms.getOrDefault(roomId, null);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants();
    }

    public Set<String> getUserEmails(UUID roomId) {
        Room room = rooms.getOrDefault(roomId, null);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants().stream()
                .map(ClientSession::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getUsernamesInRoom(UUID roomId) {
        Room room = rooms.getOrDefault(roomId, null);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants().stream().map(ClientSession::getUsername).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Integer getPlayerCount(UUID roomId) {
        Room room = rooms.getOrDefault(roomId, null);

        if (room == null) {
            return 0;
        }

        return room.size();
    }

    public Integer getReadyPlayerCount(UUID roomId) {
        return 0;
    }

    protected ClientSession getClientSessionByGuid(UUID guid) {
        return sessionManager.getByGuid(guid);
    }
}
