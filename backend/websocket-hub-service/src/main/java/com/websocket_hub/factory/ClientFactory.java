package com.websocket_hub.factory;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@Component
public class ClientFactory implements ObjectFactory<ClientSession> {

    @Override
    public ClientSession create(Object... objects) {
        if (objects.length != 4
                || !(objects[0] instanceof UUID guid)
                || !(objects[1] instanceof UserInternalResponse user)
                || !(objects[2] instanceof WebSocketSession session)
                || !(objects[3] instanceof Instant connectedAt)) {
            throw new IllegalArgumentException("Invalid arguments for ClientSession creation");
        }

        return create(guid, user, session, connectedAt);
    }

    private ClientSession create(
            UUID guid,
            UserInternalResponse user,
            WebSocketSession session,
            Instant connectedAt
    ) {
        return ClientSession.builder()
                .guid(guid)
                .username(user.username())
                .email(user.email())
                .role(user.role())
                .status(user.status())
                .session(session)
                .connectedAt(connectedAt)
                .build();
    }
}
