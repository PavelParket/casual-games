package com.security_starter.config;

import com.security_starter.factory.PermissionContextFactory;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.jwt.JwtDecoder;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.provider.DefaultPermissionProvider;
import com.security_starter.redis.RedisConfig;
import com.security_starter.redis.RedisProperties;
import com.security_starter.repository.RedisPermissionRepository;
import com.security_starter.validator.JwtValidator;
import com.security_starter.validator.PermissionValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({JwtProperties.class, RedisProperties.class})
@Import({PermissionContextFactory.class, JwtClaimsExtractor.class, JwtDecoder.class,
        DefaultPermissionProvider.class, RedisConfig.class, RedisPermissionRepository.class,
        PermissionValidator.class, JwtValidator.class})
public class SecurityAutoConfiguration {
}
