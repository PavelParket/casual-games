package com.security_starter.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(

        String secret,

        Long accessExpiration,

        Long refreshExpiration,

        String algorithm,

        List<String> publicPaths
) {
}
