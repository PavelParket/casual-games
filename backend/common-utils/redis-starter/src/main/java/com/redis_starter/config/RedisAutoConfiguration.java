package com.redis_starter.config;

import com.redis_starter.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({RedisProperties.class})
@Import({
        RedisConfig.class,
        RedisRepository.class
})
@RequiredArgsConstructor
public class RedisAutoConfiguration {
}
