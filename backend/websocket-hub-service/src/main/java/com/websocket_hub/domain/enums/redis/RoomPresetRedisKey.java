package com.websocket_hub.domain.enums.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomPresetRedisKey {

    HORSE_RACE_PRESET("preset:horse-race");

    private final String redisKey;
}
