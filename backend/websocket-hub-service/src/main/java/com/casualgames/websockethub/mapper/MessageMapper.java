package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.message.DefaultMessage;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.EventType;

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
