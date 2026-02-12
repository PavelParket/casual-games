package com.redis_starter.config;

import com.redis_starter.repository.RedisHashRepository;
import com.redis_starter.repository.RedisSetRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({RedisProperties.class})
@Import({
        RedisConfig.class,
        RedisHashRepository.class,
        RedisSetRepository.class
})
public class RedisAutoConfiguration {
}
