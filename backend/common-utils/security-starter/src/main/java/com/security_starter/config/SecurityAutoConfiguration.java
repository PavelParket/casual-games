package com.security_starter.config;

import com.redis_starter.repository.RedisHashRepository;
import com.security_starter.exception.JwtAccessDeniedHandler;
import com.security_starter.exception.JwtAuthenticationEntryPoint;
import com.security_starter.exception.SecurityExceptionHandler;
import com.security_starter.helper.PermissionContextHelper;
import com.security_starter.jwt.HmacJwtKeyProvider;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.jwt.JwtDecoder;
import com.security_starter.jwt.JwtKeyProvider;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.jwt.filter.JwtAuthenticationFilter;
import com.security_starter.provider.DefaultPermissionProvider;
import com.security_starter.provider.PermissionProvider;
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
@EnableConfigurationProperties({JwtProperties.class, ServiceWhitelistProperties.class})
@Import({
        DefaultSecurityFilterChain.class,
        PermissionValidator.class, PermissionContextHelper.class,
        JwtAccessDeniedHandler.class, JwtAuthenticationEntryPoint.class, SecurityExceptionHandler.class,
        JwtAuthenticationFilter.class, JwtDecoder.class,
        JwtClaimsExtractor.class, JwtValidator.class
})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PermissionProvider.class)
    public PermissionProvider permissionProvider(RedisHashRepository redisHashRepository) {
        return new DefaultPermissionProvider(redisHashRepository);
    }

    @Bean
    @ConditionalOnMissingBean(JwtKeyProvider.class)
    public JwtKeyProvider jwtKeyProvider(JwtProperties jwtProperties) {
        return new HmacJwtKeyProvider(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceWhitelistChecker.class)
    public ServiceWhitelistChecker serviceWhitelistChecker(ServiceWhitelistProperties properties) {
        return new ServiceWhitelistChecker(properties);
    }
}
