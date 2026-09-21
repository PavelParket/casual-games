package com.casualgames.securityservice.factory;

import com.casualgames.securityservice.jwt.JwtGenerator;
import com.casualgames.securitystarter.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenFactory {

    private final JwtGenerator tokenFactory;

    public String createAccessToken(UUID guid, String email, List<String> roles, Status status, UUID sid) {
        return tokenFactory.generateAccessToken(guid, email, roles, status, sid);
    }

    public String createRefreshToken(UUID guid, UUID sid) {
        return tokenFactory.generateRefreshToken(guid, sid);
    }
}
