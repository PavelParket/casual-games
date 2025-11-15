package com.websocket_hub.domain.dto;

public interface Message {

    String type();

    String event();

    String fromUserId();

    String toUserId();

    String roomName();

    String message();
}
