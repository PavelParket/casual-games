package casualgames.apigateway.jwt;

import javax.crypto.SecretKey;

public interface JwtKeyProvider {

    SecretKey getSigningKey();
}
