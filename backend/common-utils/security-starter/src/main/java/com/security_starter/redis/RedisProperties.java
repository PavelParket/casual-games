package com.security_starter.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(

        String host,

        Integer port,

        String password,

        Integer database
) {
}
