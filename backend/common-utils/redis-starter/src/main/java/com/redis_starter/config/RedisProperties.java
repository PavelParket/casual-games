package com.redis_starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true")
public record RedisProperties(

        String host,

        Integer port,

        String password,

        Integer database
) {
}
