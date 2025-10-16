package com.websocket_hub.manager;

import com.websocket_hub.entity.ClientSession;
import com.websocket_hub.factory.ObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

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
            return;
        }

        sessions.put(userId, factory.create(userId, username, session));

        log.info("User \"{}\" registered session \"{}\"", userId, session.getId());
    }

    public void remove(String userId) {
        ClientSession client = sessions.remove(userId);

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

    public boolean isActive(String userId) {
        ClientSession client = sessions.get(userId);

        return client != null && client.getSession().isOpen();
    }
}
