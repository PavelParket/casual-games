package com.game_service.common.service;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameType;
import com.game_service.common.exception.NotFoundException;
import com.game_service.common.service.provider.GameCleanupProvider;
import com.game_service.common.service.provider.GameMatchesProvider;
import com.kafka_starter.dto.event.RoomDeleteEvent;
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

    private final Map<GameType, GameMatchesProvider> gameMatchesProviders;

    private final Map<GameType, GameCleanupProvider> gameCleanupProviders;

    public GameService(
            List<GameMatchesProvider> gameMatchesProviderList,
            List<GameCleanupProvider> gameCleanupProviderList
    ) {
        this.gameMatchesProviders = gameMatchesProviderList.stream()
                .collect(Collectors.toMap(
                        GameMatchesProvider::gameType,
                        Function.identity())
                );
        this.gameCleanupProviders = gameCleanupProviderList.stream()
                .collect(Collectors.toMap(
                        GameCleanupProvider::gameType,
                        Function.identity())
                );
    }

    public Page<GameMatchResponse> getMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        GameMatchesProvider provider = gameMatchesProviders.get(gameMatchRequestFilter.gameType());

        if (provider == null) {
            throw new NotFoundException(String.format(GAME_TYPE_NOT_FOUND, gameMatchRequestFilter.gameType()));
        }

        return provider.findMatches(userGuid, gameMatchRequestFilter, pageable);
    }

    public void handleRoomDeleted(RoomDeleteEvent event) {
        UUID roomId = UUID.fromString(event.getRoomId());

        GameType gameType;

        try {
            gameType = GameType.valueOf(event.getRoomType());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown room type for cleanup, skipping: roomType={}, roomId={}", event.getRoomType(), roomId);
            return;
        }

        GameCleanupProvider provider = gameCleanupProviders.get(gameType);

        if (provider == null) {
            log.warn("No cleanup handler registered for gameType={}, roomId={}", gameType, roomId);
            return;
        }

        log.info("Handling room deleted event: roomId={}, gameType={}, reason={}", roomId, gameType, event.getReason());
        provider.cleanup(roomId);
    }
}
