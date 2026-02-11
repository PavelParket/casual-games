package com.websocket_hub.domain.enums.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomParticipantsRedisKey {

    ROOM_PARTICIPANTS("roomParticipants");

    private final String redisKey;
}
