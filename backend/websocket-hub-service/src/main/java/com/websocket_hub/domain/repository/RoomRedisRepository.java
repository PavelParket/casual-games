package com.websocket_hub.domain.repository;

import com.redis_starter.repository.RedisHashRepository;
import com.redis_starter.repository.RedisSetRepository;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.redis.RoomParticipantsRedisKey;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.serializer.RedisDeserializer;
import com.websocket_hub.serializer.RedisSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoomRedisRepository {

    public static final String ASTERIX_PLACEHOLDER = "*";
    public static final String COLON_PLACEHOLDER = ":";

    private final RedisHashRepository redisHashRepository;

    private final RedisSetRepository redisSetRepository;

    private final RedisSerializer redisSerializer;

    private final RedisDeserializer redisDeserializer;

    public void save(RoomMetadata roomMetadata, RoomTypeRedisKey roomTypeRedisKey) {
        try {
            String key = roomTypeRedisKey.getRedisKey();
            String hashKey = roomMetadata.getId().toString();
            String value = redisSerializer.serialize(roomMetadata);

            redisHashRepository.put(key, hashKey, value);

            log.info("Saved room metadata: roomId={}, type={}", roomMetadata.getId(), roomMetadata.getType());
        } catch (Exception e) {
            log.error("Failed to serialize room {} metadata: {}", roomMetadata.getId(), e.getMessage());
            throw new RuntimeException("Failed to save room metadata", e);
        }
    }

    public RoomMetadata get(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        try {
            String key = roomTypeRedisKey.getRedisKey();
            String hashKey = roomId.toString();
            String value = redisHashRepository.findByKey(key, hashKey);

            if (value == null) {
                log.warn("Room metadata not found: roomId={}, type={}", roomId, roomTypeRedisKey);
                return null;
            }

            return redisDeserializer.deserialize(value, RoomMetadata.class);
        } catch (Exception e) {
            log.error("Failed to deserialize room {} metadata: {}", roomId, e.getMessage());
            return null;
        }
    }

    public Set<RoomMetadata> getAll(RoomTypeRedisKey roomTypeRedisKey) {
        try {
            String key = roomTypeRedisKey.getRedisKey();
            Map<String, String> rooms = redisHashRepository.findAll(key);

            return rooms.values().stream()
                    .map(room -> redisDeserializer.deserialize(room, RoomMetadata.class))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata: {}", e.getMessage());
            return Set.of();
        }
    }

    public void delete(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        String key = roomTypeRedisKey.getRedisKey();
        String hashKey = roomId.toString();

        Long deleted = redisHashRepository.delete(key, hashKey);

        log.info("Deleted room metadata: roomId={}, type={}, deleted={}", roomId, roomTypeRedisKey, deleted);
    }

    public void updateParticipantCount(UUID roomId, RoomTypeRedisKey roomTypeRedisKey, Long count) {
        RoomMetadata roomMetadata = get(roomId, roomTypeRedisKey);

        if (roomMetadata == null) {
            log.warn("Cannot update participant count - room not found: roomId={}", roomId);
            return;
        }

        roomMetadata.setParticipantCount(count.intValue());
        save(roomMetadata, roomTypeRedisKey);

        log.info("Updated participant count: roomId={}, count={}", roomId, count);
    }

    public boolean roomExists(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        String key = roomTypeRedisKey.getRedisKey();
        String hashKey = roomId.toString();

        return redisHashRepository.hasKey(key, hashKey);
    }

    public void addParticipant(UUID roomId, UUID participantId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        Long added = redisSetRepository.add(key, participantId.toString());

        if (added > 0) {
            log.info("Added participant: roomId={}, participantId={}", roomId, participantId);
        } else {
            log.info("Participant already in room: roomId={}, participantId={}", roomId, participantId);
        }
    }

    public void removeParticipant(UUID roomId, UUID participantId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        Long removed = redisSetRepository.remove(key, participantId.toString());

        if (removed > 0) {
            log.info("Removed participant: roomId={}, participantId={}", roomId, participantId);
        } else {
            log.warn("Participant not found in room: roomId={}, participantId={}", roomId, participantId);
        }
    }

    public Set<UUID> getParticipants(UUID roomId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        return redisSetRepository.get(key).stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    public Map<UUID, Set<UUID>> getParticipantsByRoom() {
        Set<String> keys = redisSetRepository.getKeys(RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey() + COLON_PLACEHOLDER + ASTERIX_PLACEHOLDER);

        if (keys.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<String>> participants = redisSetRepository.getValuesByKey(keys);

        return keys.stream()
                .collect(Collectors.toMap(
                        key -> UUID.fromString(key.substring(key.lastIndexOf(COLON_PLACEHOLDER) + 1)),
                        key -> participants.get(key).stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toSet())
                ));
    }

    public Long getParticipantCount(UUID roomId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        return redisSetRepository.size(key);
    }

    public boolean isParticipant(UUID roomId, UUID participantId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        return redisSetRepository.contains(key, participantId.toString());
    }

    public void clearParticipants(UUID roomId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        boolean deleted = redisSetRepository.deleteKey(key);

        if (deleted) {
            log.info("Cleared all participants: roomId={}", roomId);
        } else {
            log.warn("No participants to clear: roomId={}", roomId);

        }
    }

    public boolean participantsExist(UUID roomId) {
        String key = buildRoomParticipantsRedisKey(roomId);

        return redisSetRepository.hasKey(key);
    }

    public void deleteFullRoom(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        delete(roomId, roomTypeRedisKey);
        clearParticipants(roomId);

        log.info("Completely deleted room: roomId={}, type={}", roomId, roomTypeRedisKey);
    }

    public void addParticipantAndUpdateCount(UUID roomId, UUID participantId, RoomTypeRedisKey roomTypeRedisKey) {
        addParticipant(roomId, participantId);

        Long count = getParticipantCount(roomId);

        updateParticipantCount(roomId, roomTypeRedisKey, count);
    }

    public void removeParticipantAndUpdateCount(UUID roomId, UUID participantId, RoomTypeRedisKey roomTypeRedisKey) {
        removeParticipant(roomId, participantId);

        Long count = getParticipantCount(roomId);

        updateParticipantCount(roomId, roomTypeRedisKey, count);
    }

    private String buildRoomParticipantsRedisKey(UUID roomId) {
        return String.format("%s:%s", RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey(), roomId.toString());
    }
}
