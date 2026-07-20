package com.game_service.mahjong.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game_service.common.exception.GameInternalException;
import com.game_service.mahjong.domain.entity.Mahjong;
import com.redis_starter.repository.RedisHashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MahjongGameRedisRepository {

    private static final String MAHJONG_GAMES_KEY = "mahjong:games";

    private final RedisHashRepository redisHashRepository;

    private final ObjectMapper objectMapper;

    public void save(UUID roomId, Mahjong mahjong) {
        try {
            String value = objectMapper.writeValueAsString(mahjong);
            redisHashRepository.put(MAHJONG_GAMES_KEY, roomId.toString(), value);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Mahjong game: roomId={}", roomId, e);
            throw new GameInternalException("Failed to save Mahjong game state");
        }
    }

    public Optional<Mahjong> find(UUID roomId) {
        String value = redisHashRepository.findByKey(MAHJONG_GAMES_KEY, roomId.toString());

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, Mahjong.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize Mahjong game: roomId={}", roomId, e);
            return Optional.empty();
        }
    }

    public void delete(UUID roomId) {
        redisHashRepository.delete(MAHJONG_GAMES_KEY, roomId.toString());
        log.debug("Deleted Mahjong game: roomId={}", roomId);
    }
}
