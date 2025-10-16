package com.websocket_hub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClientSession {

    @EqualsAndHashCode.Include
    private final String userId;

    @EqualsAndHashCode.Include
    private final String username;

    private final WebSocketSession session;

    @Builder.Default
    @EqualsAndHashCode.Include
    private Instant joinedAt = Instant.now();
}
