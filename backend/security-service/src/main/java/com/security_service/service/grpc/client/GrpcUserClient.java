package com.security_service.service.grpc.client;

import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.common_utils.exception.GrpcStatusExceptionMapper;
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
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public void create(CreateUserRequest createUserRequest) {
        try {
            userServiceBlockingStub.createUser(createUserRequest);
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to user-service failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw grpcStatusExceptionMapper.toException(e);
        }
    }
}
