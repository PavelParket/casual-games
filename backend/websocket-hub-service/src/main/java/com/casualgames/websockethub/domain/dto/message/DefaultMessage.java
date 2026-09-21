package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.EventType;
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
