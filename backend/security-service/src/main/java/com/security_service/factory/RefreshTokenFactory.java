package com.security_service.factory;

import com.security_service.jwt.JwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenFactory implements Factory<String> {

    private final JwtGenerator tokenFactory;

    @Override
    public String create(Object... args) {
        UUID guid = (UUID) args[0];

        return tokenFactory.generateRefreshToken(guid);
    }
}
