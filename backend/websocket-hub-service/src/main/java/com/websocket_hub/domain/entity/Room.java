package com.websocket_hub.domain.entity;

import com.websocket_hub.domain.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @EqualsAndHashCode.Include
    private final UUID id;

    @EqualsAndHashCode.Include
    private final String name;

    @EqualsAndHashCode.Include
    private final RoomType type;

    @Builder.Default
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void add(ClientSession clientSession) {
        this.participants.add(clientSession);
    }

    public void remove(ClientSession clientSession) {
        this.participants.remove(clientSession);
    }

    public boolean isEmpty() {
        return this.participants.isEmpty();
    }

    public Integer size() {
        return this.participants.size();
    }

    public List<String> getParticipantEmails() {
        return this.participants.stream()
                .map(ClientSession::getEmail)
                .toList();
    }

    public List<UUID> getParticipantGuids() {
        return this.participants.stream()
                .map(ClientSession::getGuid)
                .toList();
    }
}
