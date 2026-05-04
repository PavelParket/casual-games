package com.security_service.repository;

import com.redis_starter.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BlockedTokenRedisRepository {

    private static final String BLOCKED_TOKEN = "blocked_token:";

    private final RedisRepository redisRepository;

    public void block(String email, String token, Duration timeout) {
        redisRepository.set(BLOCKED_TOKEN + email, token, timeout);

        log.debug("Token blocked for email={}, ttl={}s", email, timeout.getSeconds());
    }
}
