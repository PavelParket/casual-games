package com.websocket_hub.domain.enums.events;

public enum HorseRaceEvent implements EventType {

    JOIN,
    LEAVE,
    READY,
    START,
    TICK,
    RESULT;

    @Override
    public String join() {
        return JOIN.name();
    }

    @Override
    public String leave() {
        return LEAVE.name();
    }
}
