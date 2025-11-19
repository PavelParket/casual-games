package com.game_service.de_coder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GameConfig {
    @Bean
    public Map<String, Long> userCooldowns() {
        return new ConcurrentHashMap<>();
    }
}
