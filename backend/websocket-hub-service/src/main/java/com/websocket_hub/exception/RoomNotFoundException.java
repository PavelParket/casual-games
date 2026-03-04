package com.websocket_hub.exception;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(UUID roomId) {
        super("Room not found: id=" + roomId);
    }
}
