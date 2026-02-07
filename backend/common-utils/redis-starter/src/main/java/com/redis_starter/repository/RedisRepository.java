package com.redis_starter.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class RedisRepository {

    private final HashOperations<String, String, String> hashOperations;

    public RedisRepository(RedisOperations<String, String> redisOperations) {
        this.hashOperations = redisOperations.opsForHash();
    }

    public boolean hasKey(String key, String hashKey) {
        if (key == null || hashKey == null) {
            return false;
        }
        return hashOperations.hasKey(key, hashKey);
    }

    public boolean add(String key, String hashKey, String value) {
        return hashOperations.putIfAbsent(key, hashKey, value);
    }

    public void addAll(String key, Map<String, String> values) {
        if (key == null || values == null || values.isEmpty()) {
            return;
        }
        hashOperations.putAll(key, values);
    }

    public void put(String key, String hashKey, String value) {
        if (key == null || hashKey == null) {
            return;
        }
        hashOperations.put(key, hashKey, value);
    }

    public boolean update(String key, String hashKey, String newValue) {
        if (!hasKey(key, hashKey)) {
            return false;
        }

        hashOperations.put(key, hashKey, newValue);

        return true;
    }

    public Long updateAll(String key, Map<String, String> values) {
        Long updated = 0L;

        if (key == null || values == null || values.isEmpty()) {
            return updated;
        }

        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (hasKey(key, entry.getKey())) {
                hashOperations.put(key, entry.getKey(), entry.getValue());
                updated++;
            }
        }

        return updated;
    }

    public String findByKey(String key, String hashKey) {
        if (key == null || hashKey == null) {
            return null;
        }

        return hashOperations.get(key, hashKey);
    }

    public Map<String, String> findAll(String key) {
        if (key == null) {
            return Collections.emptyMap();
        }

        return hashOperations.entries(key);
    }

    public List<String> findAllValues(String key) {
        if (key == null) {
            return List.of();
        }

        return hashOperations.values(key);
    }

    public Long delete(String key, String hashKey) {
        if (key == null || hashKey == null) {
            return 0L;
        }

        return hashOperations.delete(key, hashKey);
    }

    public boolean deleteByKey(String key) {
        if (key == null) {
            return false;
        }

        return Boolean.TRUE.equals(hashOperations.getOperations().delete(key));
    }

    public Long deleteList(String key, Collection<String> hashKeys) {
        if (key == null || hashKeys == null || hashKeys.isEmpty()) {
            return 0L;
        }

        return hashOperations.delete(key, hashKeys.toArray());
    }
}
