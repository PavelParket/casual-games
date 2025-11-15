package com.websocket_hub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration() {{
            setAllowedOrigins(List.of("http://localhost:5173"));
            setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            setAllowedHeaders(List.of("*"));
            setExposedHeaders(List.of("Authorization"));
            setAllowCredentials(true);
        }};

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource() {{
            registerCorsConfiguration("/**", configuration);
        }};

        return new CorsFilter(source);
    }
}
