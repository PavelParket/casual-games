package com.redis_starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.host());
        configuration.setPort(redisProperties.port());
        configuration.setDatabase(redisProperties.database());

        if (redisProperties.password() != null && !redisProperties.password().isBlank()) {
            configuration.setPassword(RedisPassword.of(redisProperties.password()));
        }

        return new LettuceConnectionFactory(configuration);
    }
}
