package com.websocket_hub.domain.context;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class WebSocketContext {

    private final UserInternalResponse user;

    private final UUID roomId;

    private final WebSocketSession session;

    private final Instant connectedAt;
}
