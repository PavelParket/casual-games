package com.websocket_hub.domain.repository;

import com.redis_starter.repository.RedisRepository;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.redis.RoomParticipantsRedisKey;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.serializer.RedisDeserializer;
import com.websocket_hub.serializer.RedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class RoomRedisRepository extends RedisRepository {

    private final Long LONG_ZERO = 0L;

    private final RedisSerializer redisSerializer;

    private final RedisDeserializer redisDeserializer;

    public RoomRedisRepository(
            RedisOperations<String, String> redisOperations,
            RedisSerializer redisSerializer,
            RedisDeserializer redisDeserializer
    ) {
        super(redisOperations);
        this.redisSerializer = redisSerializer;
        this.redisDeserializer = redisDeserializer;
    }

    public void save(RoomMetadata roomMetadata, RoomTypeRedisKey roomTypeRedisKey) {
        try {
            String key = roomTypeRedisKey.getRedisKey();
            String hashKey = roomMetadata.getId().toString();
            String value = redisSerializer.serialize(roomMetadata);

            super.put(key, hashKey, value);

            log.info("Saved room metadata: roomId={}, type={}", roomMetadata.getId(), roomMetadata.getType());
        } catch (Exception e) {
            log.error("Failed to serialize room metadata: roomId={}", roomMetadata.getId());
            throw new RuntimeException("Failed to save room metadata", e);
        }
    }

    public RoomMetadata get(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        try {
            String key = roomTypeRedisKey.getRedisKey();
            String hashKey = roomId.toString();
            String value = super.findByKey(key, hashKey);

            if (value == null) {
                log.warn("Room metadata not found: roomId={}, type={}", roomId, roomTypeRedisKey);
                return null;
            }

            return redisDeserializer.deserialize(value, RoomMetadata.class);
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata: roomId={}", roomId);
            return null;
        }
    }

    public Set<RoomMetadata> getAll(RoomTypeRedisKey roomTypeRedisKey) {
        String key = roomTypeRedisKey.getRedisKey();
        Map<String, String> allRooms = super.findAll(key);

        return allRooms.values().stream()
                .map(json -> {
                    try {
                        return redisDeserializer.deserialize(json, RoomMetadata.class);
                    } catch (Exception e) {
                        log.error("Failed to deserialize room metadata");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void delete(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        String key = roomTypeRedisKey.getRedisKey();
        String hashKey = roomId.toString();

        Long deleted = super.delete(key, hashKey);

        log.info("Deleted room metadata: roomId={}, type={}, deleted={}", roomId, roomTypeRedisKey, deleted);
    }

    public void updateParticipantCount(UUID roomId, RoomTypeRedisKey roomTypeRedisKey, int count) {
        RoomMetadata roomMetadata = get(roomId, roomTypeRedisKey);

        if (roomMetadata == null) {
            log.warn("Cannot update participant count - room not found: roomId={}", roomId);
            return;
        }

        roomMetadata.setParticipantCount(count);
        save(roomMetadata, roomTypeRedisKey);

        log.info("Updated participant count: roomId={}, count={}", roomId, count);
    }

    public void addParticipant(UUID roomId, UUID participantId) {
        try {
            String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
            String hashKey = roomId.toString();

            @SuppressWarnings("unchecked")
            List<UUID> participants = redisDeserializer.deserialize(findByKey(key, hashKey), List.class);

            if (!participants.isEmpty()) {
                participants.add(participantId);
                super.put(key, hashKey, redisSerializer.serialize(participants));

                return;
            }

            super.add(key, hashKey, redisSerializer.serialize(new ArrayList<>(List.of(participantId))));
        } catch (Exception e) {
            log.error("Failed to serialize or deserialize room participants: roomId={}", roomId);
            throw new RuntimeException("Failed to save room metadata", e);
        }
    }

    public void removeParticipant(UUID roomId, UUID participantGuid) {
        try {
            String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
            String hashKey = roomId.toString();

            @SuppressWarnings("unchecked")
            List<UUID> participants = redisDeserializer.deserialize(findByKey(key, hashKey), List.class);

            if (!participants.isEmpty()) {
                return;
            }

            Long removed = super.delete(key, hashKey);

            log.info("Removed participant: roomId={}, participantGuid={}, removed={}", roomId, participantGuid, removed);
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata while removing: roomId={}", roomId);
            throw new RuntimeException("Failed to remove participant", e);
        }
    }

    public Set<UUID> getParticipants(UUID roomId) {
        try {
            String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
            String hashKey = roomId.toString();

            @SuppressWarnings("unchecked")
            Set<UUID> participants = redisDeserializer.deserialize(super.findByKey(key, hashKey), Set.class);

            return participants;
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata while getting: roomId={}", roomId);
            throw new RuntimeException("Failed to get participants", e);
        }
    }

    public Long getParticipantCount(UUID roomId) {
        try {
            String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
            String hashKey = roomId.toString();

            return (long) redisDeserializer.deserialize(super.findByKey(key, hashKey), Set.class).size();
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata while getting count: roomId={}", roomId);
            throw new RuntimeException("Failed to get participant count", e);
        }
    }

    public boolean isParticipant(UUID roomId, UUID participantId) {
        try {
            String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
            String hashKey = roomId.toString();

            @SuppressWarnings("unchecked")
            Set<UUID> participants = redisDeserializer.deserialize(super.findByKey(key, hashKey), Set.class);

            return participants.contains(participantId);
        } catch (Exception e) {
            log.error("Failed to deserialize room metadata while checking: roomId={}", roomId);
            throw new RuntimeException("Failed to check is participant", e);
        }
    }

    public void clearParticipants(UUID roomId) {
        String key = RoomParticipantsRedisKey.ROOM_PARTICIPANTS.getRedisKey();
        String hashKey = roomId.toString();

        Long deleted = super.delete(key, hashKey);

        log.info("Cleared participants: roomId={}, deleted={}", roomId, deleted);
    }

    public void deleteRoom(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        delete(roomId, roomTypeRedisKey);
        clearParticipants(roomId);

        log.info("Completely deleted room: roomId={}, type={}", roomId, roomTypeRedisKey);
    }

    public boolean roomExists(UUID roomId, RoomTypeRedisKey roomTypeRedisKey) {
        String key = roomTypeRedisKey.getRedisKey();
        String hashKey = roomId.toString();

        return super.hasKey(key, hashKey);
    }
}
