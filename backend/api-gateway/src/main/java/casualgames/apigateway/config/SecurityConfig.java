package casualgames.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .requestCache((requestCache) -> requestCache.requestCache(NoOpServerRequestCache.getInstance()))
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/security-service/auth/**").permitAll()
                        .pathMatchers("/user-service/users/**").hasAnyAuthority("USER", "ADMIN")
                        .pathMatchers("/game-service/game/**").hasAnyAuthority("USER", "ADMIN")
                        .pathMatchers("/websocket-service/websocket/**").hasAuthority("ADMIN")
                        .anyExchange().authenticated()
                )
                .build();
    }
}

