package com.websocket_hub.domain.enums.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomTypeRedisKey {

    TIC_TAC_TOE_ROOM("ticTacToeRoom");

    private final String redisKey;
}
