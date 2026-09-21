package com.casualgames.gameservice.common.dto;

import com.casualgames.gameservice.common.enums.GameType;

import java.time.Instant;
import java.util.UUID;

public interface GameMatchResponse {

    Long id();

    GameType gameType();

    UUID roomId();

    Instant createdAt();
}
