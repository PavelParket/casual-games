package com.casualgames.websockethub.domain.context;

import com.casualgames.websockethub.domain.dto.client.UserInternalResponse;
import lombok.Builder;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WebSocketContext(

        UserInternalResponse user,

        UUID roomId,

        WebSocketSession session,

        Instant connectedAt
) {

    public static WebSocketContext of(UserInternalResponse user, UUID roomId, WebSocketSession session, Instant connectedAt) {
        return new WebSocketContext(user, roomId, session, connectedAt);
    }
}
