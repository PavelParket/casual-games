package com.casualgames.gameservice.common.service;

import com.casualgames.gameservice.common.dto.GameMatchRequestFilter;
import com.casualgames.gameservice.common.dto.GameMatchResponseList;
import com.casualgames.gameservice.common.enums.GameType;
import com.casualgames.gameservice.common.exception.NotFoundException;
import com.casualgames.gameservice.common.mapper.GameMatchMapper;
import com.casualgames.gameservice.common.service.provider.GameCleanupProvider;
import com.casualgames.gameservice.common.service.provider.GameMatchesProvider;
import com.casualgames.kafkastarter.dto.event.RoomDeleteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.casualgames.gameservice.config.ResourceMessageConstants.GAME_TYPE_NOT_FOUND;

@Service
@Slf4j
public class GameService {

    private final Map<GameType, GameMatchesProvider> gameMatchesProviders;

    private final Map<GameType, GameCleanupProvider> gameCleanupProviders;

    private final GameMatchMapper gameMatchMapper;

    public GameService(
            List<GameMatchesProvider> gameMatchesProviderList,
            List<GameCleanupProvider> gameCleanupProviderList,
            GameMatchMapper gameMatchMapper
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
        this.gameMatchMapper = gameMatchMapper;
    }

    public GameMatchResponseList getMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        GameMatchesProvider provider = gameMatchesProviders.get(gameMatchRequestFilter.gameType());

        if (provider == null) {
            throw new NotFoundException(String.format(GAME_TYPE_NOT_FOUND, gameMatchRequestFilter.gameType()));
        }

        return GameMatchResponseList.builder()
                .gameMatches(gameMatchMapper.toPagedModel(
                        provider.findMatches(userGuid, gameMatchRequestFilter, pageable)
                ))
                .build();
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
