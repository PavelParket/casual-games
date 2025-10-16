package com.websocket_hub.manager;

import com.websocket_hub.dto.Message;
import com.websocket_hub.entity.ClientSession;
import com.websocket_hub.entity.Room;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public abstract class AbstractRoomManager {

    @Getter
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private final MessageSerializer<String> serializer;

    private final ObjectFactory<Room> roomFactory;

    private final ObjectFactory<ClientSession> clientFactory;

    public abstract String getName();

    public void addSession(String roomName, String userId, String username, WebSocketSession session) {
        Room room = rooms.computeIfAbsent(roomName, roomFactory::create);
        room.add(clientFactory.create(userId, username, session));

        onAddSession(roomName, session);

        log.info("Session \"{}\" joined room \"{}\"", session.getId(), roomName);
    }

    public void removeSession(String roomName, WebSocketSession session) {
        Room room = rooms.get(roomName);

        if (room != null) {
            room.getParticipants().removeIf(clientSession -> clientSession.getSession().equals(session));

            if (room.isEmpty()) {
                rooms.remove(roomName);
            }
        }

        onRemoveSession(roomName, session);

        log.info("Session \"{}\" left room \"{}\"", session.getId(), roomName);
    }

    public void broadcast(String roomName, Message<String> message) {
        try {
            String json = serializer.serialize(message);

            Room room = rooms.get(roomName);

            if (room == null || room.isEmpty()) {
                return;
            }

            for (ClientSession clientSession : room.getParticipants()) {
                WebSocketSession session = clientSession.getSession();

                if (session == null || !session.isOpen()) {
                    continue;
                }

                Thread.ofVirtual().start(() -> {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                    }
                });
            }

            log.info("Broadcast in room \"{}\" from {} → {} recipients", roomName, message.fromUserId(), room.getParticipants().size());
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
        }
    }

    public Set<String> getActiveRooms() {
        return rooms.keySet();
    }

    public Set<String> getUsersIds(String roomName) {
        Room room = rooms.get(roomName);

        if (room == null) {
            return Set.of();
        }

        return room.getParticipants().stream()
                .map(ClientSession::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    protected void sendToSession(WebSocketSession session, Message<String> message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                session.sendMessage(new TextMessage(serializer.serialize(message)));
            } catch (Exception e) {
                log.warn("Failed to send private message to session \"{}\": {}", session.getId(), e.getMessage());
            }
        });
    }

    protected abstract void onAddSession(String roomName, WebSocketSession session);

    protected abstract void onRemoveSession(String roomName, WebSocketSession session);
}
