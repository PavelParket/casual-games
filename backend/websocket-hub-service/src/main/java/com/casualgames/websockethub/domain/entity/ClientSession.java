package com.casualgames.websockethub.domain.entity;

import com.casualgames.securitystarter.enums.Role;
import com.casualgames.securitystarter.enums.Status;
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

    private final String username;

    private final String email;

    private final Role role;

    private final Status status;

    private final WebSocketSession session;

    private final String linkProfilePicture;

    private final String linkProfilePictureMini;

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
