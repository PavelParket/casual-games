package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicTacToeGameEvent implements EventType {

    START("start"),
    READY("ready"),
    MOVE("move"),
    WINNER_X("winner X"),
    WINNER_O("winner O"),
    DRAW("draw");

    private final String description;
}
