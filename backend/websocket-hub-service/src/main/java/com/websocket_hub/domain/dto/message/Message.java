package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.enums.MessageType;

import java.util.UUID;

public interface Message<T> {

    MessageType type();

    T event();

    UUID fromUserId();

    UUID toUserId();

    UUID roomId();

    String message();
}
