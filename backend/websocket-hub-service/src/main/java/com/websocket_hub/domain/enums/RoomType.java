package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {

    TIC_TAC_TOE("Tic Tac Toe", "t-t-t"),
    DE_CODER("De-Coder", "de-coder"),
    HORSE_RACE("Horse Race", "horse-race"),
    ROOM_TEST("Room Test", "room");

    private final String label;

    private final String handlerUrl;
}
