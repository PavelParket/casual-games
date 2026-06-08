package com.redis_starter.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@Slf4j
public class RedisRepository {

    private final RedisOperations<String, String> redisOperations;

    public RedisRepository(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    public void set(String key, String value) {
        if (key == null || value == null) {
            log.warn("Attempted to set with null parameters: key={}", key);
            return;
        }

        try {
            redisOperations.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Error setting value: key={}", key, e);
        }
    }

    public void set(String key, String value, Duration ttl) {
        if (key == null || value == null) {
            log.warn("Attempted to set with null parameters: key={}", key);
            return;
        }

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            log.warn("Attempted to set with invalid TTL: key={}, ttl={}", key, ttl);
            return;
        }

        try {
            redisOperations.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.error("Error setting value with TTL: key={}", key, e);
        }
    }

    public String get(String key) {
        if (key == null) {
            log.warn("Attempted to get with null key");
            return null;
        }

        try {
            return redisOperations.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting value: key={}", key, e);
            return null;
        }
    }

    public String getAndDelete(String key) {
        if (key == null) {
            log.warn("Attempted to get and delete with null key");
            return null;
        }

        try {
            return redisOperations.opsForValue().getAndDelete(key);
        } catch (Exception e) {
            log.error("Error during get and delete: key={}", key, e);
            return null;
        }
    }

    public boolean exists(String key) {
        if (key == null) {
            log.warn("Attempted to check existence with null key");
            return false;
        }

        try {
            return Boolean.TRUE.equals(redisOperations.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking existence: key={}", key, e);
            return false;
        }
    }

    public boolean delete(String key) {
        if (key == null) {
            log.warn("Attempted to delete with null key");
            return false;
        }

        try {
            return Boolean.TRUE.equals(redisOperations.delete(key));
        } catch (Exception e) {
            log.error("Error deleting key: key={}", key, e);
            return false;
        }
    }

    public Duration getExpire(String key) {
        if (key == null) {
            log.warn("Attempted to get TTL with null key");
            return null;
        }

        try {
            Long seconds = redisOperations.getExpire(key);

            if (seconds == null || seconds < 0) {
                return null;
            }

            return Duration.ofSeconds(seconds);
        } catch (Exception e) {
            log.error("Error getting TTL: key={}", key, e);
            return null;
        }
    }
}
