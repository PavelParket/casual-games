package com.kafka_starter.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDeleteEvent {

    private String eventId;

    private String roomId;

    private String roomType;

    private String reason;

    private String timestamp;
}
