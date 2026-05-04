package casualgames.apigateway.config;

import casualgames.apigateway.jwt.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class RateLimitConfig {

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> {
            String guid = exchange.getRequest().getHeaders().getFirst(JwtAuthenticationFilter.HEADER_USER_GUID);

            if (guid != null && !guid.isBlank()) {
                return Mono.just(guid);
            }

            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getHostName()
                    : "unknown";

            log.info("Rate limit key fallback to IP: {}", ip);

            return Mono.just(ip);
        };
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
}
