package com.security_service.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(
        String secret,

        Long accessExpiration,

        Long refreshExpiration
) {
}
