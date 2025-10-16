package com.websocket_hub.factory;

import com.websocket_hub.entity.ClientSession;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;

@Component
public class ClientFactory implements ObjectFactory<ClientSession> {

    @Override
    public ClientSession create(Object... objects) {
        if (objects.length != 3 ||
                !(objects[0] instanceof String userId) ||
                !(objects[1] instanceof String username) ||
                !(objects[2] instanceof WebSocketSession session)) {
            throw new IllegalArgumentException("Invalid arguments for ClientSession creation");
        }

        return create(userId, username, session);
    }

    public ClientSession create(String userId, String username, WebSocketSession session) {
        return ClientSession.builder()
                .userId(userId)
                .username(username)
                .session(session)
                .build();
    }
}
