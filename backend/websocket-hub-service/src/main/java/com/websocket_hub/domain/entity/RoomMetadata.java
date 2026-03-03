package com.websocket_hub.domain.entity;

import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomMetadata {

    private UUID id;

    private String name;

    private RoomType type;

    private Instant createdAt;

    private Integer participantCount;

    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING;

    private Instant gameFinishedAt;

    public static RoomMetadata create(Room room) {
        return RoomMetadata.builder()
                .id(room.getId())
                .name(room.getName())
                .type(room.getType())
                .createdAt(room.getCreatedAt())
                .participantCount(room.size())
                .build();
    }
}
