package com.game_service.common.dto;

import com.game_service.common.enums.GameType;

import java.time.Instant;
import java.util.UUID;

public interface GameMatchResponse {

    Long id();

    GameType gameType();

    UUID roomId();

    Instant createdAt();
}
