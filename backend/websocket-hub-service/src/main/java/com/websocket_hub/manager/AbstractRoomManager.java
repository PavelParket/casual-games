package com.websocket_hub.manager;

import com.websocket_hub.dto.Message;
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
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    private final MessageSerializer<String> serializer;

    public abstract String getName();

    public void addSession(String roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        onAddSession(roomId, session);

        log.info("Session \"{}\" joined room \"{}\"", session.getId(), roomId);
    }

    public void removeSession(String roomId, WebSocketSession session) {
        var sessions = rooms.get(roomId);

        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                rooms.remove(roomId, sessions);
            }
        }

        onRemoveSession(roomId, session);

        log.info("Session \"{}\" left room \"{}\"", session.getId(), roomId);
    }

    public void broadcast(String roomId, Message<String> message) {
        try {
            String json = serializer.serialize(message);

            var sessions = rooms.get(roomId);

            if (sessions == null || sessions.isEmpty()) {
                return;
            }

            for (WebSocketSession session : sessions) {
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

            log.info("Broadcast in room \"{}\" from {} → {} recipients", roomId, message.fromUserId(), sessions.size());
        } catch (Exception e) {
            log.error("Failed to broadcast message", e);
        }
    }

    public Set<String> getActiveRooms() {
        return rooms.keySet();
    }

    public Set<String> getUsersId(String roomId) {
        return getRooms().getOrDefault(roomId, Set.of()).stream()
                .map(session -> (String) session.getAttributes().get("userId"))
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

    protected abstract void onAddSession(String roomId, WebSocketSession session);

    protected abstract void onRemoveSession(String roomId, WebSocketSession session);
}
