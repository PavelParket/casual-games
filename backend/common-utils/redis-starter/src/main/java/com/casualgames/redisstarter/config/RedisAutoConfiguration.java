package com.casualgames.redisstarter.config;

import com.casualgames.redisstarter.repository.RedisHashRepository;
import com.casualgames.redisstarter.repository.RedisRepository;
import com.casualgames.redisstarter.repository.RedisSetRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({RedisProperties.class})
@Import({
        RedisConfig.class,
        RedisRepository.class,
        RedisHashRepository.class,
        RedisSetRepository.class
})
public class RedisAutoConfiguration {
}
