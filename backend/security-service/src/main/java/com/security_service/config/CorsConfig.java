package com.security_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration() {{
            setAllowedOrigins(List.of("http://localhost:5173"));
            setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            setAllowedHeaders(List.of("*"));
            setExposedHeaders(List.of("Authorization"));
            setAllowCredentials(true);
        }};

        return new UrlBasedCorsConfigurationSource() {{
            registerCorsConfiguration("/**", configuration);
        }};
    }
}
