package casualgames.apigateway.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class JwtClaimsExtractor {

    private final JwtDecoder jwtDecoder;

    public UUID extractGuid(String token) {
        return UUID.fromString(jwtDecoder.decode(token).getSubject());
    }

    public String extractEmail(String token) {
        return jwtDecoder.decode(token).get("email", String.class);
    }

    public List<String> extractRole(String token) {
        Claims claims = jwtDecoder.decode(token);
        Object roles = claims.get("roles");

        if (roles instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        log.warn("No roles claim found in token");

        return List.of();
    }

    public UUID extractSid(String token) {
        return UUID.fromString(jwtDecoder.decode(token).get("sid", String.class));
    }
}
