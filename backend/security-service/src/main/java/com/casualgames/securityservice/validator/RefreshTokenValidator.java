package com.casualgames.securityservice.validator;

import com.casualgames.securityservice.exception.InvalidTokenException;
import com.casualgames.securitystarter.jwt.JwtClaimsExtractor;
import com.casualgames.securitystarter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.casualgames.securityservice.config.ResourceMessageConstants.INVALID_REFRESH_TOKEN;

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
