package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.factory.ObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
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

    private final Map<UUID, ClientSession> sessions = new ConcurrentHashMap<>();

    private final ObjectFactory<ClientSession> factory;

    public void register(UUID guid, UserInfoInternalResponse user, WebSocketSession session, Instant connectedAt) {
        if (guid == null || user == null || session == null) {
            log.warn("Invalid registration attempt: userId={}, session={}", guid, session);
            return;
        }

        sessions.compute(guid, (key, client) -> {
            if (client != null && isActive(guid)) {
                try {
                    log.info("User {} already connected — closing old session {}", user.email(), client.getSession().getId());

                    client.getSession().close(CloseStatus.POLICY_VIOLATION);
                } catch (IOException e) {
                    log.warn("Failed to close previous session for user {}: {}", user.email(), e.getMessage());
                }
            }

            return factory.create(guid, user, session, connectedAt);
        });

        log.info("User \"{}\" registered session \"{}\"", user.email(), session.getId());
    }

    public void remove(UUID guid) {
        ClientSession client = sessions.remove(guid);

        if (client != null) {
            try {
                if (isActive(guid)) {
                    client.getSession().close(CloseStatus.NORMAL);
                    log.info("Closed WebSocket session {} for user {}", client.getSession().getId(), client.getEmail());
                }
            } catch (IOException e) {
                log.warn("Error closing WebSocket for user {}: {}", guid, e.getMessage());
            }

            log.info("Removed session {} for user {}", client.getSession().getId(), client.getEmail());
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

    public ClientSession getByEmail(String email) {
        if (email == null) {
            return null;
        }

        return sessions.values().stream()
                .filter(client -> email.equals(client.getEmail()) && isActive(client.getGuid()))
                .findFirst()
                .orElse(null);
    }

    public boolean isActive(UUID guid) {
        ClientSession client = sessions.get(guid);

        return client != null && client.getSession().isOpen();
    }
}
