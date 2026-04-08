package com.security_service.service.grpc.client;

import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.UserGrpc;
import com.security_service.exception.GrpcGlobalExceptionHandler;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcUserClient {

    @GrpcClient("user-service")
    private UserGrpc.UserBlockingStub userBlockingStub;

    public void create(CreateUserRequest createUserRequest) {
        try {
            userBlockingStub.createUser(createUserRequest);
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to user-service failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw GrpcGlobalExceptionHandler.mapToServiceException(e);
        }
    }
}
