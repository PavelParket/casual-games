package com.security_service.factory;

import com.security_service.jwt.JwtProperties;
import com.security_service.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenFactory implements Factory<String> {

    private final JwtProperties properties;

    private final JwtProvider jwtProvider;

    @Override
    public String create(Object... args) {
        UUID guid = (UUID) args[0];

        return jwtProvider.generateToken(guid, properties.accessExpiration());
    }
}
