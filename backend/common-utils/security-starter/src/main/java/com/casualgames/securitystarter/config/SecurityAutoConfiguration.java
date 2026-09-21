package com.casualgames.securitystarter.config;

import com.casualgames.redisstarter.repository.RedisHashRepository;
import com.casualgames.securitystarter.exception.JwtAccessDeniedHandler;
import com.casualgames.securitystarter.exception.JwtAuthenticationEntryPoint;
import com.casualgames.securitystarter.exception.SecurityExceptionHandler;
import com.casualgames.securitystarter.helper.PermissionContextHelper;
import com.casualgames.securitystarter.jwt.HmacJwtKeyProvider;
import com.casualgames.securitystarter.jwt.JwtClaimsExtractor;
import com.casualgames.securitystarter.jwt.JwtDecoder;
import com.casualgames.securitystarter.jwt.JwtKeyProvider;
import com.casualgames.securitystarter.jwt.JwtProperties;
import com.casualgames.securitystarter.jwt.filter.JwtAuthenticationFilter;
import com.casualgames.securitystarter.provider.DefaultPermissionProvider;
import com.casualgames.securitystarter.provider.PermissionProvider;
import com.casualgames.securitystarter.validator.JwtValidator;
import com.casualgames.securitystarter.validator.PermissionValidator;
import com.casualgames.securitystarter.whitelist.ServiceWhitelistChecker;
import com.casualgames.securitystarter.whitelist.ServiceWhitelistProperties;
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
