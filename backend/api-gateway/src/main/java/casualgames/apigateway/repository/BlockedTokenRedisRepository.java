package casualgames.apigateway.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BlockedTokenRedisRepository {

    private static final String BLOCKED_TOKEN = "blocked_token:";

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public Mono<Boolean> isBlocked(String email, String token) {
        return reactiveStringRedisTemplate.opsForValue()
                .get(BLOCKED_TOKEN + email)
                .map(token::equals)
                .switchIfEmpty(Mono.just(false))
                .onErrorResume(e -> {
                    log.error("Redis error checking blocked token for email={}: {}", email, e.getMessage());

                    return Mono.just(false);
                });
    }
}
