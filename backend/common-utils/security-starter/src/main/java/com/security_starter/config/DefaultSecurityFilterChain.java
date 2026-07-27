package com.security_starter.config;

import com.security_starter.exception.JwtAccessDeniedHandler;
import com.security_starter.exception.JwtAuthenticationEntryPoint;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class DefaultSecurityFilterChain {

    private static final List<String> DEFAULT_PUBLIC_PATHS = List.of(
            "/actuator/health",
            "/actuator/info"
    );

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    private final JwtAccessDeniedHandler accessDeniedHandler;

    private final JwtProperties jwtProperties;

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return DefaultHttpSecurityConfig
                .defaultHttpSecurity(httpSecurity, jwtAuthenticationFilter, authenticationEntryPoint, accessDeniedHandler)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(publicPathsArray()).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    private String[] publicPathsArray() {
        List<String> publicPathList = new ArrayList<>(DEFAULT_PUBLIC_PATHS);
        if (jwtProperties.publicPaths() != null) {
            publicPathList.addAll(jwtProperties.publicPaths());
        }
        return publicPathList.toArray(String[]::new);
    }
}
