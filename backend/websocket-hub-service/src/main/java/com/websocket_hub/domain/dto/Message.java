package com.websocket_hub.domain.dto;

public interface Message<T> {

    String type();

    String fromUserId();

    String toUserId();

    String roomId();

    T content();
}
