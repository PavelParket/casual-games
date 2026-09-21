package com.casualgames.websockethub.domain.enums.events;

public enum HorseRaceEvent implements EventType {

    JOIN,
    LEAVE,
    READY,
    START,
    TICK,
    RESULT,
    BET,
    BET_REJECT,
    BET_REQUIRED,
    COUNTDOWN,
    CANCELED;

    @Override
    public String join() {
        return JOIN.name();
    }

    @Override
    public String leave() {
        return LEAVE.name();
    }
}
