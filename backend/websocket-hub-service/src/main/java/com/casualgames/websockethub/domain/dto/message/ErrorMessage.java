package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.enums.ErrorCode;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.ErrorEvent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ErrorMessage(

        MessageType type,

        ErrorEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        ErrorCode errorCode,

        Instant timestamp

) implements Message<ErrorEvent> {
}
