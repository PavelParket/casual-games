package com.websocket_hub.exception;

import com.websocket_hub.domain.enums.RoomType;

public class RoomTypeNotFoundException extends RuntimeException {

    public RoomTypeNotFoundException(RoomType roomType) {
        super("No manager found for room type: " + roomType);
    }
}
