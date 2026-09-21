package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.enums.MessageType;

import java.util.UUID;

public interface Message<T> {

    MessageType type();

    T event();

    UUID fromUserId();

    UUID toUserId();

    UUID roomId();

    String message();
}
