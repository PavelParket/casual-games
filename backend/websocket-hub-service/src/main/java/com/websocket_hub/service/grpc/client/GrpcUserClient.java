package com.websocket_hub.service.grpc.client;

import com.casualgames.grpc.user.GetByGuidRequest;
import com.casualgames.grpc.user.UserResponse;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.common_utils.exception.GrpcStatusExceptionMapper;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
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
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public UserInternalResponse getByGuid(UUID guid) {
        GetByGuidRequest request = GetByGuidRequest.newBuilder()
                .setGuid(guid.toString())
                .build();

        try {
            UserResponse response = userServiceBlockingStub.getByGuid(request);

            return mapToInternal(response);
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetByGuid failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw grpcStatusExceptionMapper.toException(e);
        }
    }

    private UserInternalResponse mapToInternal(UserResponse response) {
        return UserInternalResponse.builder()
                .guid(UUID.fromString(response.getGuid()))
                .username(response.getUsername())
                .email(response.getEmail())
                .balance(new BigDecimal(response.getBalance()))
                .role(response.getRole())
                .status(response.getStatus())
                .build();
    }
}
