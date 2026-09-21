package com.casualgames.websockethub.domain.enums.events;

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
