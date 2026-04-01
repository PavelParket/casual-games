package com.security_service.service.grpc.client;

import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.UserGrpc;
import com.security_service.exception.ServiceUnavailableException;
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
            throw mapToServiceException(e);
        }
    }

    private ServiceUnavailableException mapToServiceException(StatusRuntimeException e) {
        String description = e.getStatus().getDescription() != null
                ? e.getStatus().getDescription()
                : "Unknown gRPC error";

        return switch (e.getStatus().getCode()) {
            case ALREADY_EXISTS, INVALID_ARGUMENT -> new ServiceUnavailableException(description);

            case UNAVAILABLE -> new ServiceUnavailableException("User service is unavailable");

            default -> new ServiceUnavailableException("Failed to create user: " + description);
        };
    }
}
