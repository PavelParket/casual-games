package com.security_starter.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(
        String secret,

        Long accessExpiration,

        Long refreshExpiration
) {
}
