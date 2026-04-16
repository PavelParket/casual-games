package com.game_service.common.service.provider;

import com.game_service.common.enums.GameType;

import java.util.UUID;

public interface GameCleanupProvider {

    GameType gameType();

    void cleanup(UUID roomId);
}
