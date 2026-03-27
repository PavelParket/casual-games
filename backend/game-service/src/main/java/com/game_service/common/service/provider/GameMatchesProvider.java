package com.game_service.common.service.provider;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GameMatchesProvider {

    String TO_JSON = "[\"%s\"]";

    GameType gameType();

    Page<GameMatchResponse> findMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable);
}
