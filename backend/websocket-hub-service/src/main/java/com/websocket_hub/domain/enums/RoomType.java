package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {

    TIC_TAC_TOE("Tic Tac Toe", "t-t-t", false),
    DE_CODER("De-Coder", "de-coder", true),
    HORSE_RACE("Horse Race", "horse-race", false),
    DURAK("Durak", "durak", false),
    ROOM_TEST("Room Test", "room", false);

    private final String label;

    private final String handlerUrl;

    private final boolean allowsLateJoin;
}
