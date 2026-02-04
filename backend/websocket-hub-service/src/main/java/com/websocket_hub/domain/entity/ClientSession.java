package com.websocket_hub.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClientSession {

    @EqualsAndHashCode.Include
    private final UUID guid;

    @EqualsAndHashCode.Include
    private final String username;

    @EqualsAndHashCode.Include
    private final String email;

    @EqualsAndHashCode.Include
    private final String role;

    @EqualsAndHashCode.Include
    private final String status;

    private final WebSocketSession session;

    @Builder.Default
    private Instant connectedAt = Instant.now();

    public boolean validateSession(WebSocketSession session) {
        return this.getSession().equals(session);
    }

    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        this.session.sendMessage(message);
    }

    public boolean isOpen() {
        return this.session.isOpen();
    }
}
