package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.EventType;
import com.websocket_hub.domain.enums.MessageType;
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
