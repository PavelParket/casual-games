package com.websocket_hub.domain.enums.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomTypeRedisKey {

    TIC_TAC_TOE_ROOM("room:tic-tac-toe"),
    DE_CODER_ROOM("room:de-coder"),
    HORSE_RACE_ROOM("room:horse-race");

    private final String redisKey;
}
