package com.casualgames.grpcutils.config;

import com.casualgames.grpcutils.properties.GrpcClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcConfig {

    private final GrpcClientProperties grpcProperties;

    public Duration getDefaultGrpcTimeout() {
        return Duration.ofSeconds(grpcProperties.getTimeoutSeconds());
    }

    public Consumer<? super Throwable> getDefaultGrpcErrorLogger() {
        return error -> log.error("Error while gRPC request sending", error);
    }
}
