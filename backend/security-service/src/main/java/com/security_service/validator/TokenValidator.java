package com.security_service.validator;

import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenValidator implements Validator {

    private final JwtParser parser;

    public boolean isExpired(String token) {
        return parser.parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }
}
