package com.grpc_utils.interceptor;

import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

import java.time.Duration;
import java.util.Optional;

public class CommonGrpcLogger {

    protected long getMillisSinceStart(long start) {
        return Duration.ofNanos(System.nanoTime() - start).toMillis();
    }

    protected String getMethodName(ServerCall<?, ?> serverCall) {
        return Optional.ofNullable(serverCall)
                .map(ServerCall::getMethodDescriptor)
                .map(this::getMethodName)
                .orElse(null);
    }

    protected String getMethodName(MethodDescriptor<?, ?> methodDescriptor) {
        return Optional.ofNullable(methodDescriptor)
                .map(MethodDescriptor::getFullMethodName)
                .orElse(null);
    }

}
