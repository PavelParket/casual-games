package com.grpc_utils.interceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@GrpcGlobalClientInterceptor
@Slf4j
public class GrpcClientLoggingInterceptor extends CommonGrpcLogger implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {

        UUID callId = UUID.randomUUID();

        final long start = System.nanoTime();

        return new ForwardingClientCall.SimpleForwardingClientCall<>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                log.debug("gRPC client - message sending, method: {}, time: {} ms, call id: {}", getMethodName(methodDescriptor), getMillisSinceStart(start), callId);
                super.sendMessage(message);
                log.debug("gRPC client - message sent, method: {}, time: {} ms, call id: {}", getMethodName(methodDescriptor), getMillisSinceStart(start), callId);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {

                    @Override
                    public void onMessage(RespT message) {
                        log.debug("gRPC client - message received, method: {}, time: {} ms, call id: {}", getMethodName(methodDescriptor), getMillisSinceStart(start), callId);
                        super.onMessage(message);
                    }
                }, headers);
            }
        };
    }
}
