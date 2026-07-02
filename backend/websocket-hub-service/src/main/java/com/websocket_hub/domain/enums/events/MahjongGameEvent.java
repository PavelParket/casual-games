package com.websocket_hub.domain.enums.events;

public enum MahjongGameEvent implements EventType {

    JOIN,
    LEAVE,
    START,
    START_FAILED,
    READY,
    MOVE,
    GAME_STATE,
    GAME_OVER,
    DEADLOCK_WAIT,
    BET,
    BET_REJECT,
    BET_OUTBID,
    BET_REQUIRED;

    @Override
    public String join() {
        return JOIN.name();
    }

    @Override
    public String leave() {
        return LEAVE.name();
    }
}
