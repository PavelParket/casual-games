package com.security_starter.config;

import com.security_starter.factory.PermissionContextFactory;
import com.security_starter.temp.RolePermissionRepository;
import com.security_starter.validator.PermissionValidator;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ConditionalOnClass(EntityManager.class)
@EntityScan(basePackages = "com.security_starter.temp")
@EnableJpaRepositories(basePackages = "com.security_starter.temp")
@EnableAspectJAutoProxy
@Import({PermissionContextFactory.class})
public class SecurityStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionValidator permissionValidator(RolePermissionRepository rolePermissionRepository) {
        return new PermissionValidator(rolePermissionRepository);
    }
}
