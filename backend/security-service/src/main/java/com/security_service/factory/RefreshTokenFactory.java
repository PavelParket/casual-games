package com.security_service.factory;

import com.security_service.jwt.JwtProperties;
import com.security_service.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenFactory implements Factory<String> {

    private final JwtProperties properties;

    private final JwtProvider jwtProvider;

    @Override
    public String create(Object... args) {
        String username = (String) args[0];
        String email = (String) args[1];
        String role = (String) args[2];

        return jwtProvider.generateToken(username, email, role, properties.refreshExpiration());
    }
}
