package com.websocket_hub.domain.dto.event;

import java.util.UUID;

public record CountdownExpiredEvent(

        UUID roomId

) {
}
