package com.security_starter.config;

import com.security_starter.factory.PermissionContextFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableAspectJAutoProxy
@Import({PermissionContextFactory.class})
public class SecurityAutoConfiguration {
}
