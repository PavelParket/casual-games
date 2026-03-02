package com.game_service.de_coder.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeCoderGameEvent {

    START,
    MOVE,
    WINNER,
    LOSER;
}
