package com.security_starter.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis")
public record RedisProperties(String host, Integer port, String password, Integer database) {

    public RedisProperties {
        if (host == null || host.isBlank()) {
            host = "localhost";
        }
        if (port == null) {
            port = 6379;
        }
        if (database == null) {
            database = 0;
        }
    }
}
