package com.websocket_hub.domain.enums.events;

public enum DurakGameEvent implements EventType {

    JOIN,
    LEAVE,
    BET,
    BET_REJECT,
    BET_OUTBID,
    BET_REQUIRED,
    READY,
    START,
    START_FAILED,
    MOVE,
    GAME_STATE,
    GAME_OVER,
    TIMER_UPDATE;

    @Override
    public String join() {
        return JOIN.name();
    }

    @Override
    public String leave() {
        return LEAVE.name();
    }
}
