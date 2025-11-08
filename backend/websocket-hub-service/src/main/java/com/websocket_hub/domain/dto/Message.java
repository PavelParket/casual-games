package com.websocket_hub.domain.dto;

public interface Message<T> {

    String type();

    String event();

    String fromUserId();

    String toUserId();

    String roomName();

    T content();
}
