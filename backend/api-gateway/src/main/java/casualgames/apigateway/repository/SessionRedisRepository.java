package casualgames.apigateway.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SessionRedisRepository {

    private static final String SESSION_KEY_PREFIX = "session";
    private static final String SESSION_KEY_FORMAT = "%s:%s:%s";

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isActive(UUID guid, UUID sid) {
        String key = String.format(SESSION_KEY_FORMAT, SESSION_KEY_PREFIX, guid, sid);

        return redisTemplate.hasKey(key)
                .onErrorResume(e -> {
                    log.error("Redis error checking session: guid={}, sid={}: {}", guid, sid, e.getMessage());
                    return Mono.just(false);
                });
    }
}
