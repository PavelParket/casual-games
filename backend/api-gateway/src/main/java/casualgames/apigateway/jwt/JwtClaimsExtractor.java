package casualgames.apigateway.jwt;

import com.common_utils.enums.Role;
import com.common_utils.enums.Status;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public Role extractRole(String token) {
        Claims claims = jwtDecoder.decode(token);
        String roleStr = claims.get("role", String.class);

        try {
            return roleStr != null ? Role.valueOf(roleStr) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role in token: {}", roleStr);

            return null;
        }
    }

    public Status extractStatus(String token) {
        Claims claims = jwtDecoder.decode(token);
        String statusStr = claims.get("status", String.class);

        try {
            return statusStr != null ? Status.valueOf(statusStr) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown status in token: {}", statusStr);

            return null;
        }
    }
}
