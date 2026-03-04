package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ErrorMessage(

        MessageType type,

        ErrorEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String errorCode,

        String message

) implements Message<ErrorEvent> {
}
