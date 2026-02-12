package com.redis_starter.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class RedisHashRepository {

    private final Long LONG_ZERO = 0L;

    protected final HashOperations<String, String, String> hashOperations;

    public RedisHashRepository(RedisOperations<String, String> redisOperations) {
        this.hashOperations = redisOperations.opsForHash();
    }

    public boolean hasKey(String key, String hashKey) {
        if (key == null || hashKey == null) {
            log.warn("Attempted to check key existence with null parameters: key={}, hashKey={}", key, hashKey);
            return false;
        }

        try {
            return hashOperations.hasKey(key, hashKey);
        } catch (Exception e) {
            log.error("Error checking key existence: key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    public Boolean exists(String key) {
        if (key == null) {
            log.warn("Attempted to check key existence with null key");
            return false;
        }

        try {
            return hashOperations.getOperations().hasKey(key);
        } catch (Exception e) {
            log.error("Error checking key existence: key={}", key, e);
            return false;
        }
    }

    public boolean add(String key, String hashKey, String value) {
        if (key == null || hashKey == null) {
            log.warn("Attempted to add with null parameters: key={}, hashKey={}", key, hashKey);
            return false;
        }

        try {
            return hashOperations.putIfAbsent(key, hashKey, value);
        } catch (Exception e) {
            log.error("Error adding value: key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    public void addAll(String key, Map<String, String> values) {
        if (key == null || values == null || values.isEmpty()) {
            log.warn("Attempted to add all with invalid parameters: key={}, valuesSize={}", key, values != null ? values.size() : 0);
            return;
        }

        try {
            hashOperations.putAll(key, values);
        } catch (Exception e) {
            log.error("Error adding multiple values: key={}", key, e);
        }
    }

    public void put(String key, String hashKey, String value) {
        if (key == null || hashKey == null) {
            log.warn("Attempted to put with null parameters: key={}, hashKey={}", key, hashKey);
            return;
        }

        try {
            hashOperations.put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Error putting value: key={}, hashKey={}", key, hashKey, e);
        }
    }

    public boolean update(String key, String hashKey, String newValue) {
        if (!hasKey(key, hashKey)) {
            return false;
        }

        try {
            hashOperations.put(key, hashKey, newValue);
            return true;
        } catch (Exception e) {
            log.error("Error updating value: key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    public Long updateAll(String key, Map<String, String> values) {
        if (key == null || values == null || values.isEmpty()) {
            return LONG_ZERO;
        }

        try {
            Map<String, String> existing = hashOperations.entries(key);
            Map<String, String> toUpdate = values.entrySet().stream()
                    .filter(e -> existing.containsKey(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!toUpdate.isEmpty()) {
                hashOperations.putAll(key, toUpdate);
            }

            return (long) toUpdate.size();
        } catch (Exception e) {
            log.error("Error updating multiple values: key={}", key, e);
            return LONG_ZERO;
        }
    }

    public String findByKey(String key, String hashKey) {
        if (key == null || hashKey == null) {
            log.warn("Attempted to find value with null parameters: key={}, hashKey={}", key, hashKey);
            return null;
        }

        try {
            return hashOperations.get(key, hashKey);
        } catch (Exception e) {
            log.error("Error finding value: key={}, hashKey={}", key, hashKey, e);
            return null;
        }
    }

    public Map<String, String> findAll(String key) {
        if (key == null) {
            log.warn("Attempted to find all with null key");
            return Collections.emptyMap();
        }

        try {
            return hashOperations.entries(key);
        } catch (Exception e) {
            log.error("Error finding all values: key={}", key, e);
            return Collections.emptyMap();
        }
    }

    public List<String> findAllValues(String key) {
        if (key == null) {
            log.warn("Attempted to find all values with null key");
            return List.of();
        }

        try {
            return hashOperations.values(key);
        } catch (Exception e) {
            log.error("Error finding all values: key={}", key, e);
            return List.of();
        }
    }

    public Long delete(String key, String hashKey) {
        if (key == null || hashKey == null) {
            log.warn("Attempted to delete with null parameters: key={}, hashKey={}", key, hashKey);
            return LONG_ZERO;
        }

        try {
            return hashOperations.delete(key, hashKey);
        } catch (Exception e) {
            log.error("Error deleting value: key={}, hashKey={}", key, hashKey, e);
            return LONG_ZERO;
        }
    }

    public boolean deleteByKey(String key) {
        if (key == null) {
            log.warn("Attempted to delete key with null key");
            return false;
        }

        try {
            return Boolean.TRUE.equals(hashOperations.getOperations().delete(key));
        } catch (Exception e) {
            log.error("Error deleting key: key={}", key, e);
            return false;
        }
    }

    public Long deleteList(String key, Collection<String> hashKeys) {
        if (key == null || hashKeys == null || hashKeys.isEmpty()) {
            log.warn("Attempted to delete list with invalid parameters: key={}", key);
            return LONG_ZERO;
        }

        try {
            return hashOperations.delete(key, hashKeys.toArray());
        } catch (Exception e) {
            log.error("Error deleting list: key={}, count={}", key, hashKeys.size(), e);
            return LONG_ZERO;
        }
    }

    public Long size(String key) {
        if (key == null) {
            log.warn("Attempted to get size with null key");
            return LONG_ZERO;
        }

        try {
            return hashOperations.size(key);
        } catch (Exception e) {
            log.error("Error getting size: key={}", key, e);
            return LONG_ZERO;
        }
    }

    public Set<String> hashKeys(String key) {
        if (key == null) {
            log.warn("Attempted to get keys with null key");
            return Collections.emptySet();
        }

        try {
            return hashOperations.keys(key);
        } catch (Exception e) {
            log.error("Error getting keys: key={}", key, e);
            return Collections.emptySet();
        }
    }
}
