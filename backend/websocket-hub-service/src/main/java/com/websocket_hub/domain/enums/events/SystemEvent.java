package com.websocket_hub.domain.enums.events;

public enum SystemEvent implements EventType {

    PING,
    PONG;

    @Override
    public String join() {
        return null;
    }

    @Override
    public String leave() {
        return null;
    }
}
