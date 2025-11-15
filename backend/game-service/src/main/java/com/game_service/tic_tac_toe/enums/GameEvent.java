package com.game_service.tic_tac_toe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameEvent {

    START("start"),
    MOVE("move"),
    WINNER_X("winner X"),
    WINNER_O("winner O"),
    DRAW("draw");

    private final String description;
}
