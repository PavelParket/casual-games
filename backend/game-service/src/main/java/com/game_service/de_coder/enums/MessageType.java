package com.game_service.de_coder.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    START("Start"),
    MOVE("Move"),
    WINNER("You win!"),
    LOSER("You lose!");

    private final String type;
}
