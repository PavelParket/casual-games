package com.websocket_hub.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @EqualsAndHashCode.Include
    private final String id;

    @EqualsAndHashCode.Include
    private final String name;

    @Builder.Default
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void add(ClientSession clientSession) {
        participants.add(clientSession);
    }

    public void remove(ClientSession clientSession) {
        participants.remove(clientSession);
    }

    public boolean isEmpty() {
        return participants.isEmpty();
    }
}
