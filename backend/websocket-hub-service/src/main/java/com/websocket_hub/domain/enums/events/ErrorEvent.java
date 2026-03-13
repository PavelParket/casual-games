package com.websocket_hub.domain.enums.events;

public enum ErrorEvent implements EventType {

    ERROR;

    @Override
    public String join() {
        return null;
    }

    @Override
    public String leave() {
        return null;
    }
}
