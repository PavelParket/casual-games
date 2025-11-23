package com.websocket_hub.domain.enums;

import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.TicTacToeGameRoomManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {

    TIC_TAC_TOE(TicTacToeGameRoomManager.class),
    ROOM_TEST(RoomManager.class);

    private final Class<? extends AbstractRoomManager> managerClass;
}
