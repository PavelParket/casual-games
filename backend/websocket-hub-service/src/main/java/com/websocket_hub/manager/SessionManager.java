package com.websocket_hub.manager;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.factory.ObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManager {

    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();

    private final ObjectFactory<ClientSession> factory;

    public void register(String userId, String username, WebSocketSession session) {
        if (userId == null || username == null || session == null) {
            log.warn("Invalid registration attempt: userId={}, username={}, session={}", userId, username, session);
            return;
        }

        sessions.compute(userId, (key, client) -> {
            if (client != null && client.getSession().isOpen()) {
                try {
                    log.info("User {} already connected — closing old session {}", userId, client.getSession().getId());

                    client.getSession().close(CloseStatus.POLICY_VIOLATION);
                } catch (IOException e) {
                    log.warn("Failed to close previous session for user {}: {}", userId, e.getMessage());
                }
            }

            return factory.create(userId, username, session);
        });

        log.info("User \"{}\" registered session \"{}\"", userId, session.getId());
    }

    public void remove(String userId) {
        ClientSession client = sessions.remove(userId);

        if (client != null) {
            try {
                WebSocketSession session = client.getSession();

                if (session != null && session.isOpen()) {
                    session.close(CloseStatus.NORMAL);
                    log.debug("Closed WebSocket session {} for user {}", session.getId(), userId);
                }
            } catch (IOException e) {
                log.warn("Error closing WebSocket for user {}: {}", userId, e.getMessage());
            }
        }

        log.info("User {} removed session {}", userId, client != null ? client.getSession().getId() : null);
    }

    public Map<String, ClientSession> getAll() {
        return sessions;
    }

    public ClientSession getByUserId(String userId) {
        ClientSession client = sessions.get(userId);

        if (client != null && !client.getSession().isOpen()) {
            sessions.remove(userId);

            return null;
        }

        return client;
    }

    public ClientSession getByUsername(String username) {
        if (username == null) {
            return null;
        }

        return sessions.values().stream()
                .filter(client -> username.equals(client.getUsername()))
                .filter(client -> client.getSession() != null && client.getSession().isOpen())
                .findFirst()
                .orElse(null);
    }

    public boolean isActive(String userId) {
        ClientSession client = sessions.get(userId);

        return client != null && client.getSession().isOpen();
    }
}
