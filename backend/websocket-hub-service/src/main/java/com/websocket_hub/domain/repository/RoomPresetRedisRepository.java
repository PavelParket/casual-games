package com.websocket_hub.domain.repository;

import com.redis_starter.repository.RedisHashRepository;
import com.websocket_hub.domain.enums.redis.RoomPresetRedisKey;
import com.websocket_hub.serializer.RedisDeserializer;
import com.websocket_hub.serializer.RedisSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoomPresetRedisRepository {

    private final RedisHashRepository redisHashRepository;

    private final RedisSerializer redisSerializer;

    private final RedisDeserializer redisDeserializer;

    public <T> void savePreset(UUID roomId, T preset, RoomPresetRedisKey presetRedisKey) {
        try {
            String key = presetRedisKey.getRedisKey();
            String hashKey = roomId.toString();
            String value = redisSerializer.serialize(preset);

            redisHashRepository.put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Failed to save preset for room={}: {}", roomId, e.getMessage());
            throw new RuntimeException("Failed to save preset", e);
        }
    }

    public <T> T getPreset(UUID roomId, RoomPresetRedisKey presetRedisKey, Class<T> clazz) {
        try {
            String key = presetRedisKey.getRedisKey();
            String hashKey = roomId.toString();
            String value = redisHashRepository.findByKey(key, hashKey);

            if (value == null) {
                log.warn("Preset not found for room={}, preset redis key={}", roomId, presetRedisKey);
                return null;
            }

            return redisDeserializer.deserialize(value, clazz);
        } catch (Exception e) {
            log.error("Failed to get preset for room={}: {}", roomId, e.getMessage());
            return null;
        }
    }

    public void deletePreset(UUID roomId, RoomPresetRedisKey presetRedisKey) {
        String key = presetRedisKey.getRedisKey();
        String hashKey = roomId.toString();

        redisHashRepository.delete(key, hashKey);

        log.info("Deleted preset for room={}, preset redis key={}", roomId, presetRedisKey);
    }
}
