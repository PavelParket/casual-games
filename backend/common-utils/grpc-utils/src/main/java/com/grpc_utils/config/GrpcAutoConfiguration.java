package com.grpc_utils.config;

import com.grpc_utils.interceptor.GrpcClientLoggingInterceptor;
import com.grpc_utils.interceptor.GrpcServerLoggingInterceptor;
import com.grpc_utils.properties.GrpcClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(GrpcClientProperties.class)
@Import({
        GrpcClientLoggingInterceptor.class,
        GrpcServerLoggingInterceptor.class,
        GrpcConfig.class
})
public class GrpcAutoConfiguration {
}
