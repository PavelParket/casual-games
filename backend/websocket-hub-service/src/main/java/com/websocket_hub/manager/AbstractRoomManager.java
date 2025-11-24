package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.Message;
import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.service.RoomManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public abstract class AbstractRoomManager {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private final MessageSerializer<String> serializer;

    private final SessionManager sessionManager;

    private final RoomManagerService service;

    public abstract String getName();

    protected abstract void onAddSession(UserInfoInternalResponse user, String roomName, WebSocketSession session);

    protected abstract void onRemoveSession(UserInfoInternalResponse user, String roomName, WebSocketSession session);

    public boolean validateManagerType(RoomType roomType) {
        return this.getClass().equals(roomType.getManagerClass());
    }

    // todo: разделить логику между создание комнаты и входом в неё
    public void addSession(String roomName, RoomType roomType, UserInfoInternalResponse user, WebSocketSession session) {
        Room room = service.getOrCreate(roomName, roomType, rooms);
        ClientSession client = sessionManager.getByGuid(user.guid());

        if (client != null && client.validateSession(session) && validateManagerType(roomType)) {
            synchronized (room) {
                room.add(client);
            }
        }

        onAddSession(user, roomName, session);

        log.info("Session \"{}\" [user \"{}\"] joined room \"{}\"", session.getId(), user.email(), roomName);
    }

    public void removeSession(String roomName, RoomType roomType, UserInfoInternalResponse user, WebSocketSession session) {
        ClientSession client = sessionManager.getByGuid(user.guid());
        Room room = rooms.getOrDefault(roomName, null);

        if (client != null && client.validateSession(session) && room != null && room.getType().equals(roomType)) {
            synchronized (room) {
                room.remove(client);

                if (room.isEmpty()) {
                    log.info("Room \"{}\" is now empty, removing...", roomName);

                    rooms.remove(roomName);
                }
            }
        }

        onRemoveSession(user, roomName, session);

        log.info("Session \"{}\" left room \"{}\"", session.getId(), roomName);
    }

    public void broadcast(String roomName, Message message) {
        try {
            String json = serializer.serialize(message);

            Room room = rooms.get(roomName);

            if (room == null || room.isEmpty()) {
                return;
            }

            Set<ClientSession> dead = ConcurrentHashMap.newKeySet();

            for (ClientSession clientSession : room.getParticipants()) {
                WebSocketSession session = clientSession.getSession();

                if (session == null || !session.isOpen()) {
                    dead.add(clientSession);

                    continue;
                }

                Thread.ofVirtual().start(() -> {
                    try {
                        session.sendMessage(new TextMessage(json));
                        log.info("Sent message: {}", json);
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());

                        dead.add(clientSession);
                    }
                });
            }

            if (!dead.isEmpty()) {
                synchronized (room) {
                    room.getParticipants().removeAll(dead);

                    if (room.isEmpty()) {
                        rooms.remove(roomName);

                        log.info("Room \"{}\" removed due to all sessions being closed", roomName);
                    }
                }
            }

            log.info("Broadcast in room \"{}\" from {} → {} recipients", roomName, message.fromUserId(), room.size());
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
        }
    }

    public List<Room> getActiveRooms() {
        return rooms.values().stream().toList();
    }

    public Set<String> getActiveRoomsNames() {
        return rooms.keySet();
    }

    public Set<String> getUserEmails(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            return Set.of();
        }

        Room room = rooms.get(roomName);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants().stream()
                .map(ClientSession::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserNames(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            return Set.of();
        }

        Room room = rooms.get(roomName);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants().stream().map(ClientSession::getUsername).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Integer getReadyPlayerCount(String roomName) {
        return 0;
    }

    protected void sendToSession(ClientSession client, Message message) {
        if (client == null || !client.isOpen()) {
            return;
        }

        try {
            client.sendMessage(new TextMessage(serializer.serialize(message)));
        } catch (Exception e) {
            log.error("Failed to send private message to session \"{}\": {}", client.getEmail(), e.getMessage());
        }
    }
}
