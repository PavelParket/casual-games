package com.casualgames.gameservice.common.service.provider;

import com.casualgames.gameservice.common.enums.GameType;

import java.util.UUID;

public interface GameCleanupProvider {

    GameType gameType();

    void cleanup(UUID roomId);
}
