package com.redis_starter.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class RedisSetRepository {

    private final Long LONG_ZERO = 0L;

    private final SetOperations<String, String> setOperations;

    public RedisSetRepository(RedisOperations<String, String> redisOperations) {
        this.setOperations = redisOperations.opsForSet();
    }

    public Long add(String key, String value) {
        if (key == null || value == null) {
            log.warn("Attempted to add with invalid parameters: key={}", key);
            return LONG_ZERO;
        }

        try {
            return setOperations.add(key, value);
        } catch (Exception e) {
            log.error("Error adding value to set: key={}", key, e);
            return LONG_ZERO;
        }
    }

    public Long addAll(String key, Collection<String> values) {
        if (key == null || values == null || values.isEmpty()) {
            log.warn("Attempted to add all with invalid parameters: key={}, size={}",
                    key, values != null ? values.size() : 0);
            return LONG_ZERO;
        }

        try {
            return setOperations.add(key, values.toArray(new String[0]));
        } catch (Exception e) {
            log.error("Error adding all values to set: key={}, size={}", key, values.size(), e);
            return LONG_ZERO;
        }
    }

    public Long remove(String key, String value) {
        if (key == null || value == null) {
            log.warn("Attempted to remove with invalid parameters: key={}", key);
            return LONG_ZERO;
        }

        try {
            return setOperations.remove(key, value);
        } catch (Exception e) {
            log.error("Error removing value from set: key={}", key, e);
            return LONG_ZERO;
        }
    }

    public Long removeAll(String key, Collection<String> values) {
        if (key == null || values == null || values.isEmpty()) {
            log.warn("Attempted to remove all with invalid parameters: key={}, size={}", key, values != null ? values.size() : 0);
            return LONG_ZERO;
        }

        try {
            return setOperations.remove(key, values.toArray());
        } catch (Exception e) {
            log.error("Error removing all values from set: key={}, size={}", key, values.size(), e);
            return LONG_ZERO;
        }
    }

    public Set<String> get(String key) {
        if (key == null) {
            log.warn("Attempted to get values with null key");
            return Set.of();
        }

        try {
            Set<String> values = setOperations.members(key);
            return values == null ? Set.of() : values;
        } catch (Exception e) {
            log.error("Error getting values from set: key={}", key, e);
            return Set.of();
        }
    }

    public Set<String> getKeys(String pattern) {
        Set<String> keys = setOperations.getOperations().keys(pattern);

        return keys != null ? keys : Set.of();
    }

    public Map<String, Set<String>> getValuesByKey(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            log.warn("Attempted to get values with null parameters: key={}", keys);
            return Map.of();
        }

        return keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        this::get
                ));
    }

    public boolean contains(String key, String value) {
        if (key == null || value == null) {
            log.warn("Attempted to check existing with null parameters: key={}, value={}", key, value);
            return false;
        }

        try {
            return Boolean.TRUE.equals(setOperations.isMember(key, value));
        } catch (Exception e) {
            log.error("Error checking existing in set: key={}, member={}", key, value, e);
            return false;
        }
    }

    public Long size(String key) {
        if (key == null) {
            log.warn("Attempted to get size with null key");
            return LONG_ZERO;
        }

        try {
            return setOperations.size(key);
        } catch (Exception e) {
            log.error("Error getting set size: key={}", key, e);
            return LONG_ZERO;
        }
    }

    public Boolean hasKey(String key) {
        if (key == null) {
            log.warn("Attempted to check existence with null key");
            return false;
        }

        try {
            return setOperations.getOperations().hasKey(key);
        } catch (Exception e) {
            log.error("Error checking set existence: key={}", key, e);
            return false;
        }
    }

    public boolean deleteKey(String key) {
        if (key == null) {
            log.warn("Attempted to delete with null key");
            return false;
        }

        try {
            return Boolean.TRUE.equals(setOperations.getOperations().delete(key));
        } catch (Exception e) {
            log.error("Error deleting set: key={}", key, e);
            return false;
        }
    }
}
