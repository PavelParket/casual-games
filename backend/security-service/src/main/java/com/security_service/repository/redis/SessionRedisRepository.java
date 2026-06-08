package com.security_service.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis_starter.repository.RedisRepository;
import com.redis_starter.repository.RedisSetRepository;
import com.security_service.domain.entity.SessionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SessionRedisRepository {

    private static final String SESSION_PREFIX = "session";
    private static final String INDEX_PREFIX = "session_index";

    private static final String SESSION_KEY_FORMAT = "%s:%s:%s";
    private static final String INDEX_KEY_FORMAT = "%s:%s";

    private final RedisRepository redisRepository;

    private final RedisSetRepository redisSetRepository;

    private final ObjectMapper objectMapper;

    public void save(UUID guid, UUID sid, SessionData data, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisRepository.set(sessionKey(guid, sid), json, ttl);
            log.debug("Session saved: guid={}, sid={}", guid, sid);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SessionData: guid={}, sid={}", guid, sid, e);
        }
    }

    public Optional<SessionData> find(UUID guid, UUID sid) {
        String json = redisRepository.get(sessionKey(guid, sid));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SessionData.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize SessionData: guid={}, sid={}", guid, sid, e);
            return Optional.empty();
        }
    }

    public void delete(UUID guid, UUID sid) {
        redisRepository.delete(sessionKey(guid, sid));
        log.debug("Session deleted: guid={}, sid={}", guid, sid);
    }

    public void indexAdd(UUID guid, UUID sid) {
        redisSetRepository.add(indexKey(guid), sid.toString());
    }

    public void indexRemove(UUID guid, UUID sid) {
        redisSetRepository.remove(indexKey(guid), sid.toString());
    }

    public Set<String> indexGetAll(UUID guid) {
        return redisSetRepository.get(indexKey(guid));
    }

    public void indexClear(UUID guid) {
        redisRepository.delete(indexKey(guid));
    }

    private String sessionKey(UUID guid, UUID sid) {
        return String.format(SESSION_KEY_FORMAT, SESSION_PREFIX, guid, sid);
    }

    private String indexKey(UUID guid) {
        return String.format(INDEX_KEY_FORMAT, INDEX_PREFIX, guid);
    }
}
