package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {

    TIC_TAC_TOE("Tic Tac Toe", "t-t-t", false, 2),
    DE_CODER("De-Coder", "de-coder", true, 50),
    HORSE_RACE("Horse Race", "horse-race", false, 50),
    DURAK("Durak", "durak", false, 2);

    private final String label;

    private final String handlerUrl;

    private final boolean allowsLateJoin;

    private final Integer maxParticipants;
}
