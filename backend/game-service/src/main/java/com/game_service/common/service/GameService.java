package com.game_service.common.service;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameType;
import com.game_service.common.exception.NotFoundException;
import com.game_service.common.service.provider.GameMatchesProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.game_service.config.ResourceMessageConstants.GAME_TYPE_NOT_FOUND;

@Service
@Slf4j
public class GameService {

    private final Map<GameType, GameMatchesProvider> gameProviders;

    public GameService(List<GameMatchesProvider> gameMatchesProviderList) {
        this.gameProviders = gameMatchesProviderList.stream()
                .collect(Collectors.toMap(
                        GameMatchesProvider::gameType,
                        Function.identity())
                );
    }

    public Page<GameMatchResponse> getMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        GameMatchesProvider provider = gameProviders.get(gameMatchRequestFilter.gameType());

        if (provider == null) {
            throw new NotFoundException(String.format(GAME_TYPE_NOT_FOUND, gameMatchRequestFilter.gameType()));
        }

        log.info("Fetching stats for gameType={}, userGuid={}, isWinner={}", gameMatchRequestFilter.gameType(), userGuid, gameMatchRequestFilter.isWinner());

        return provider.findMatches(userGuid, gameMatchRequestFilter, pageable);
    }
}
