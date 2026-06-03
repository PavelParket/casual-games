package com.security_service.validator;

import com.security_service.exception.InvalidTokenException;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.INVALID_REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class RefreshTokenValidator implements Validator {

    private final JwtValidator jwtValidator;

    private final JwtClaimsExtractor jwtClaimsExtractor;

    public RefreshClaims validate(String token) {
        if (!jwtValidator.isValid(token)) {
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN);
        }

        UUID guid = jwtClaimsExtractor.extractGuid(token);
        UUID sid = jwtClaimsExtractor.extractSid(token);

        return new RefreshClaims(guid, sid);
    }

    public record RefreshClaims(UUID guid, UUID sid) {
    }
}
