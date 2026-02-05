package com.security_starter.config;

import com.security_starter.exception.handler.JwtAccessDeniedHandler;
import com.security_starter.exception.handler.JwtAuthenticationEntryPoint;
import com.security_starter.factory.PermissionContextFactory;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.jwt.JwtDecoder;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.jwt.filter.JwtAuthenticationFilter;
import com.security_starter.provider.DefaultPermissionProvider;
import com.security_starter.provider.PermissionProvider;
import com.security_starter.redis.RedisConfig;
import com.security_starter.redis.RedisProperties;
import com.security_starter.repository.RedisPermissionRepository;
import com.security_starter.validator.JwtValidator;
import com.security_starter.validator.PermissionValidator;
import com.security_starter.whitelist.ServiceWhitelistChecker;
import com.security_starter.whitelist.ServiceWhitelistProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({JwtProperties.class, RedisProperties.class, ServiceWhitelistProperties.class})
@Import({
        CorsConfig.class, DefaultSecurityFilterChain.class,
        PermissionValidator.class, PermissionContextFactory.class,
        JwtAccessDeniedHandler.class, JwtAuthenticationEntryPoint.class, SecurityException.class,
        JwtAuthenticationFilter.class, JwtDecoder.class,
        JwtClaimsExtractor.class, JwtValidator.class,
        RedisConfig.class, RedisPermissionRepository.class,
        ServiceWhitelistChecker.class
})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PermissionProvider.class)
    public PermissionProvider permissionProvider(RedisPermissionRepository repository) {
        return new DefaultPermissionProvider(repository);
    }
}
