package com.security_starter.config;

import com.security_starter.exception.JwtAccessDeniedHandler;
import com.security_starter.exception.JwtAuthenticationEntryPoint;
import com.security_starter.jwt.filter.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public final class DefaultHttpSecurityConfig {

    private static final long HEADERS_MAX_AGE_IN_SECONDS = 31536000;

    private DefaultHttpSecurityConfig() {
    }

    public static HttpSecurity defaultHttpSecurity(HttpSecurity httpSecurity,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   JwtAuthenticationEntryPoint authenticationEntryPoint,
                                                   JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .securityContext(securityContext ->
                        securityContext.requireExplicitSave(true)
                )
                .requestCache(RequestCacheConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers ->
                        headers.httpStrictTransportSecurity(
                                httpStrictTransportSecurity -> httpStrictTransportSecurity
                                        .includeSubDomains(true)
                                        .maxAgeInSeconds(HEADERS_MAX_AGE_IN_SECONDS)
                        )
                )
                .exceptionHandling(
                        exceptionHandler -> exceptionHandler
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
