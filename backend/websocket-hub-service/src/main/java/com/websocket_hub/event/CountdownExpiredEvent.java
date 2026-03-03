package com.websocket_hub.event;

import java.util.UUID;

public record CountdownExpiredEvent(

        UUID roomId

) {
}
