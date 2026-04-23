package casualgames.apigateway.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

    private final JwtDecoder jwtDecoder;

    public boolean isValidAndNotExpired(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Claims claims = jwtDecoder.decode(token);
            Date expiration = claims.getExpiration();

            return expiration == null || !expiration.before(new Date());
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());

            return false;
        }
    }
}
