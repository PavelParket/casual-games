package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {

    TIC_TAC_TOE("TicTacToeGameRoomManager"),
    ROOM_TEST("RoomManager");

    private final String managerName;
}
