package com.game_service.tic_tac_toe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    START("Start"),
    MOVE("Move"),
    WINNER_X("Winner X"),
    WINNER_O("Winner O"),
    DRAW("Draw");

    private final String type;
}
