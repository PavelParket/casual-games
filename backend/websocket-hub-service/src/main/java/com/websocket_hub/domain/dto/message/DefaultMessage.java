package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.EventType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DefaultMessage(

        MessageType type,

        EventType event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message

) implements Message<EventType> {
}
