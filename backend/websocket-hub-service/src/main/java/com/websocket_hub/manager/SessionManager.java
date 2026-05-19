package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.events.EventType;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManager {

    private static final CloseStatus SESSION_DISPLACED = new CloseStatus(4001, "Session displaced");

    private final Map<UUID, ClientSession> sessions = new ConcurrentHashMap<>();

    private final ObjectFactory<ClientSession> factory;

    private final MessageSerializer serializer;

    public void register(UUID guid, UserInternalResponse user, WebSocketSession session, Instant connectedAt) {
        if (guid == null || user == null || session == null) {
            log.warn("Invalid registration attempt: userId={}, session={}", guid, session);
            return;
        }

        ClientSession[] displaced = new ClientSession[1];

        sessions.compute(guid, (key, existing) -> {
            displaced[0] = existing;

            return factory.create(guid, user, session, connectedAt);
        });

        if (displaced[0] != null && displaced[0].getSession().isOpen()) {
            try {
                log.info("User {} already connected — closing old session {}", user.email(), displaced[0].getSession().getId());

                displaced[0].getSession().close(SESSION_DISPLACED);
            } catch (IOException e) {
                log.warn("Failed to close previous session for user {}: {}", user.email(), e.getMessage());
            }
        }

        log.info("User \"{}\" registered session \"{}\"", user.email(), session.getId());
    }

    public void remove(UUID guid) {
        if (isActive(guid)) {
            ClientSession client = sessions.remove(guid);

            try {
                client.getSession().close(CloseStatus.NORMAL);
                log.info("Closed WebSocket session {} for user {}", client.getSession().getId(), client.getEmail());

            } catch (IOException e) {
                log.warn("Error closing WebSocket for user {}: {}", guid, e.getMessage());
            }

            log.info("Removed session {} for user {}", client.getSession().getId(), client.getEmail());
        }
    }

    public void removeIfCurrent(UUID guid, WebSocketSession session) {
        sessions.computeIfPresent(guid, (key, current) -> {
            if (current.getSession().getId().equals(session.getId())) {
                log.info("Removing current session {} for user {}", session.getId(), guid);

                return null;
            }

            log.info("Session {} was already displaced for user {} — skipping remove", session.getId(), guid);

            return current;
        });
    }

    public void sendToSession(ClientSession client, Message<? extends EventType> message) {
        if (client == null || !client.isOpen()) {
            return;
        }

        try {
            String json = serializer.serialize(message);

            client.sendMessage(new TextMessage(json));
            log.info("Sent message to user id={} name={} content={}", client.getGuid(), client.getUsername(), json);
        } catch (Exception e) {
            log.error("Failed to send private message to session \"{}\": {}", client.getEmail(), e.getMessage());
        }
    }

    public Map<UUID, ClientSession> getAll() {
        return sessions;
    }

    public ClientSession getByGuid(UUID guid) {
        ClientSession client = sessions.get(guid);

        if (!isActive(guid)) {
            sessions.remove(guid);

            return null;
        }

        return client;
    }

    public boolean isActive(UUID guid) {
        ClientSession client = sessions.get(guid);

        return client != null && client.getSession().isOpen();
    }
}
