package com.websocket_hub.dto;

public interface Message<T> {

    String type();

    String fromUserId();

    String roomId();

    T content();
}
