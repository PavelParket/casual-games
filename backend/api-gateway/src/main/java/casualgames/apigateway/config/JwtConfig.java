package casualgames.apigateway.config;

import casualgames.apigateway.jwt.HmacJwtKeyProvider;
import casualgames.apigateway.jwt.JwtClaimsExtractor;
import casualgames.apigateway.jwt.JwtDecoder;
import casualgames.apigateway.jwt.JwtKeyProvider;
import casualgames.apigateway.jwt.JwtProperties;
import casualgames.apigateway.jwt.JwtValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtKeyProvider jwtKeyProvider(JwtProperties jwtProperties) {
        return new HmacJwtKeyProvider(jwtProperties);
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtKeyProvider jwtKeyProvider) {
        return new JwtDecoder(jwtKeyProvider);
    }

    @Bean
    public JwtValidator jwtValidator(JwtDecoder jwtDecoder) {
        return new JwtValidator(jwtDecoder);
    }

    @Bean
    public JwtClaimsExtractor jwtClaimsExtractor(JwtDecoder jwtDecoder) {
        return new JwtClaimsExtractor(jwtDecoder);
    }
}
