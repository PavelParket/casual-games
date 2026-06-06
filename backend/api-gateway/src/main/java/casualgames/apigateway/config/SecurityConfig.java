package casualgames.apigateway.config;

import casualgames.apigateway.jwt.JwtClaimsExtractor;
import casualgames.apigateway.jwt.JwtProperties;
import casualgames.apigateway.jwt.JwtValidator;
import casualgames.apigateway.jwt.filter.JwtAuthenticationFilter;
import casualgames.apigateway.repository.SessionRedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtValidator jwtValidator;
    private final JwtClaimsExtractor jwtClaimsExtractor;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private final SessionRedisRepository sessionRedisRepository;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(
                objectMapper,
                jwtProperties,
                jwtValidator,
                jwtClaimsExtractor,
                sessionRedisRepository
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .requestCache(requestCache -> requestCache.requestCache(NoOpServerRequestCache.getInstance()))
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(publicPathsArray()).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private String[] publicPathsArray() {
        if (jwtProperties.publicPaths() == null || jwtProperties.publicPaths().isEmpty()) {
            return new String[]{};
        }

        return jwtProperties.publicPaths().toArray(String[]::new);
    }
}
