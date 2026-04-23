package casualgames.apigateway.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(

        String secret,

        String algorithm,

        List<String> publicPaths
) {
}
