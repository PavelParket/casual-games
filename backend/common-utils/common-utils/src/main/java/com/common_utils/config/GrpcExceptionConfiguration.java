package com.common_utils.config;

import com.common_utils.exception.GrpcGlobalExceptionHandler;
import com.common_utils.exception.GrpcStatusExceptionMapper;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(GrpcAdvice.class)
@Import({
        GrpcGlobalExceptionHandler.class,
        GrpcStatusExceptionMapper.class
})
public class GrpcExceptionConfiguration {
}
