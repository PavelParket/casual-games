package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.DefaultMessage;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.EventType;

import java.util.UUID;

public interface MessageMapper {

    default DefaultMessage toResponse(MessageType type,
                                      EventType event,
                                      UUID fromUserId,
                                      UUID toUserId,
                                      UUID roomId,
                                      String message) {
        return DefaultMessage.builder()
                .type(type).event(event)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .roomId(roomId)
                .message(message)
                .build();
    }
}
