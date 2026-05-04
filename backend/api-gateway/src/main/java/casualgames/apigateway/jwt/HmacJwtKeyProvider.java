package casualgames.apigateway.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class HmacJwtKeyProvider implements JwtKeyProvider {

    private final JwtProperties jwtProperties;

    @Override
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
