package com.security_starter.config;

import com.security_starter.exception.handler.JwtAccessDeniedHandler;
import com.security_starter.exception.handler.JwtAuthenticationEntryPoint;
import com.security_starter.exception.handler.SecurityExceptionHandler;
import com.security_starter.factory.PermissionContextFactory;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.jwt.JwtDecoder;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.jwt.filter.JwtAuthenticationFilter;
import com.security_starter.provider.DefaultPermissionProvider;
import com.security_starter.redis.RedisConfig;
import com.security_starter.redis.RedisProperties;
import com.security_starter.repository.RedisPermissionRepository;
import com.security_starter.validator.JwtValidator;
import com.security_starter.validator.PermissionValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({JwtProperties.class, RedisProperties.class})
@Import({
        RedisConfig.class,
        RedisPermissionRepository.class,
        PermissionContextFactory.class,
        JwtDecoder.class,
        JwtValidator.class,
        JwtClaimsExtractor.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class,
        SecurityExceptionHandler.class,
        DefaultPermissionProvider.class,
        PermissionValidator.class,
})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        return new JwtDecoder(properties);
    }

    @Bean
    @ConditionalOnMissingBean(JwtValidator.class)
    public JwtValidator jwtValidator(JwtDecoder decoder) {
        return new JwtValidator(decoder);
    }

    @Bean
    @ConditionalOnMissingBean(JwtClaimsExtractor.class)
    public JwtClaimsExtractor jwtClaimsExtractor(JwtDecoder decoder) {
        return new JwtClaimsExtractor(decoder);
    }
}
