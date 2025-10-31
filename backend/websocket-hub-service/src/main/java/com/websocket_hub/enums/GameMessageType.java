package com.websocket_hub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameMessageType {

    START("start"),
    MOVE("move"),
    WINNER_X("winner X"),
    WINNER_O("winner O"),
    DRAW("draw");

    private final String type;
}
