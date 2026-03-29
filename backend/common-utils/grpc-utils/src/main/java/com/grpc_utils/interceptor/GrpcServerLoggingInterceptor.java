package com.grpc_utils.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@GrpcGlobalServerInterceptor
@Slf4j
public class GrpcServerLoggingInterceptor extends CommonGrpcLogger implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        UUID callId = UUID.randomUUID();

        final long start = System.nanoTime();

        ServerCall<ReqT, RespT> listener = new ForwardingServerCall.SimpleForwardingServerCall<>(serverCall) {

            @Override
            public void sendMessage(RespT message) {
                log.debug("gRPC server - message sending, method: {}, time: {} ms, call id: {}", getMethodName(serverCall), getMillisSinceStart(start), callId);
                super.sendMessage(message);
                log.debug("gRPC server - message sent, method: {}, time: {} ms, call id: {}", getMethodName(serverCall), getMillisSinceStart(start), callId);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(serverCallHandler.startCall(listener, metadata)) {

            @Override
            public void onMessage(ReqT message) {
                log.debug("gRPC server - message received, method: {}, time: {} ms, call id: {}", getMethodName(serverCall), getMillisSinceStart(start), callId);
                super.onMessage(message);
            }
        };
    }
}
