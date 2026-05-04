package com.grpc_utils.config;

import com.grpc_utils.exception.GrpcGlobalExceptionHandler;
import com.grpc_utils.interceptor.GrpcClientLoggingInterceptor;
import com.grpc_utils.interceptor.GrpcServerLoggingInterceptor;
import com.grpc_utils.mapper.GrpcStatusExceptionMapper;
import com.grpc_utils.properties.GrpcClientProperties;
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
