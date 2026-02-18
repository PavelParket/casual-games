package com.websocket_hub.domain.enums.events;

public enum TicTacToeGameEvent implements EventType {

    JOIN,
    LEAVE,
    START,
    READY,
    MOVE,
    WINNER_X,
    WINNER_O,
    DRAW,
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
