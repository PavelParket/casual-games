package com.security_service.service.grpc.client;

import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.UserGrpc;
import com.casualgames.grpc.user.UserResponse;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import com.security_service.domain.dto.client.CreateUserInternalRequest;
import com.security_service.domain.dto.client.CreateUserInternalResponse;
import com.security_service.exception.ServiceUnavailableException;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcUserClient {

    @GrpcClient("user-service")
    private UserGrpc.UserBlockingStub userBlockingStub;

    public CreateUserInternalResponse create(CreateUserInternalRequest request) {
        CreateUserRequest grpcRequest = CreateUserRequest.newBuilder()
                .setGuid(request.guid().toString())
                .setUsername(request.username())
                .setEmail(request.email())
                .build();

        try {
            UserResponse grpcResponse = userBlockingStub.createUser(grpcRequest);

            return CreateUserInternalResponse.builder()
                    .guid(UUID.fromString(grpcResponse.getGuid()))
                    .username(grpcResponse.getUsername())
                    .email(grpcResponse.getEmail())
                    .balance(new BigDecimal(grpcResponse.getBalance()))
                    .role(grpcResponse.getRole())
                    .status(grpcResponse.getStatus())
                    .createdAt((GrpcTimestampMapper.toInstant(grpcResponse.getCreatedAt())))
                    .build();
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
