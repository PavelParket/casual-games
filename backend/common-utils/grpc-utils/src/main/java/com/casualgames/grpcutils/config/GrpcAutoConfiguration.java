package com.casualgames.grpcutils.config;

import com.casualgames.grpcutils.exception.GrpcGlobalExceptionHandler;
import com.casualgames.grpcutils.interceptor.GrpcClientLoggingInterceptor;
import com.casualgames.grpcutils.interceptor.GrpcServerLoggingInterceptor;
import com.casualgames.grpcutils.mapper.GrpcStatusExceptionMapper;
import com.casualgames.grpcutils.properties.GrpcClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(GrpcClientProperties.class)
@Import({
        GrpcClientLoggingInterceptor.class,
        GrpcServerLoggingInterceptor.class,
        GrpcConfig.class,
        GrpcGlobalExceptionHandler.class, GrpcStatusExceptionMapper.class
})
public class GrpcAutoConfiguration {
}
