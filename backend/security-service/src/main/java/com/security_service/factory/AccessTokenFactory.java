package com.security_service.factory;

import com.security_service.jwt.JwtGenerator;
import com.security_starter.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenFactory implements Factory<String> {

    private final JwtGenerator tokenFactory;

    @Override
    public String create(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Expected at least: guid, email, roles");
        }

        UUID guid = (UUID) args[0];
        String email = (String) args[1];

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) args[2];

        Status status = args.length > 3 ? (Status) args[3] : Status.DEFAULT;

        return tokenFactory.generateAccessToken(guid, email, roles, status);
    }
}
