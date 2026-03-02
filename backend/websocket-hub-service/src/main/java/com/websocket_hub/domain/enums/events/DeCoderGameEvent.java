package com.websocket_hub.domain.enums.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeCoderGameEvent implements EventType {

    JOIN,
    LEAVE,
    START,
    MOVE,
    WINNER,
    LOSER,
    ERROR,
    STATE;

    @Override
    public String join() {
        return JOIN.name();
    }

    @Override
    public String leave() {
        return LEAVE.name();
    }
}
