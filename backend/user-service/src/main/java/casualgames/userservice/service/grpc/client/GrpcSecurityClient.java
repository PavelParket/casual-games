package casualgames.userservice.service.grpc.client;

import casualgames.userservice.dto.security_service.UpdateUserInternalRequest;
import casualgames.userservice.dto.security_service.UpdateUserInternalResponse;
import casualgames.userservice.exception.ServiceUnavailableException;
import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UpdateUserRequest;
import com.casualgames.grpc.user.UpdateUserRoleRequest;
import com.casualgames.grpc.user.UpdateUserResponse;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import com.security_starter.enums.Role;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.google.common.base.Strings.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcSecurityClient {

    @GrpcClient("security-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UpdateUserInternalResponse update(UpdateUserInternalRequest request) {
        UpdateUserRequest grpcRequest = UpdateUserRequest.newBuilder()
                .setGuid(request.guid().toString())
                .setUsername(nullToEmpty(request.username()))
                .setEmail(nullToEmpty(request.email()))
                .setPassword(nullToEmpty(request.password()))
                .build();

        try {
            UpdateUserResponse grpcResponse = userServiceBlockingStub.updateUser(grpcRequest);

            return UpdateUserInternalResponse.builder()
                    .guid(UUID.fromString(grpcResponse.getGuid()))
                    .username(grpcResponse.getUsername())
                    .email(grpcResponse.getEmail())
                    .role(grpcResponse.getRole())
                    .createdAt(GrpcTimestampMapper.toInstant(grpcResponse.getCreatedAt()))
                    .build();
        } catch (StatusRuntimeException e) {
            log.error("gRPC UpdateUser failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw mapToServiceException(e, "update");
        }
    }

    public void updateRole(UUID guid, Role role) {
        UpdateUserRoleRequest grpcRequest = UpdateUserRoleRequest.newBuilder()
                .setGuid(guid.toString())
                .setRole(role.name())
                .build();

        try {
            userBlockingStub.updateUserRole(grpcRequest);
        } catch (StatusRuntimeException e) {
            log.error("gRPC UpdateUserRole failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription());
            throw mapToServiceException(e, "update role for");
        }
    }

    public void delete(UUID guid) {
        DeleteUserRequest grpcRequest = DeleteUserRequest.newBuilder()
                .setGuid(guid.toString())
                .build();

        try {
            userServiceBlockingStub.deleteUser(grpcRequest);
        } catch (StatusRuntimeException e) {
            log.error("gRPC DeleteUser failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw mapToServiceException(e, "delete");
        }
    }

    private ServiceUnavailableException mapToServiceException(StatusRuntimeException e, String operation) {
        String description = e.getStatus().getDescription() != null
                ? e.getStatus().getDescription()
                : "Unknown gRPC error";

        return switch (e.getStatus().getCode()) {
            case NOT_FOUND, ALREADY_EXISTS -> new ServiceUnavailableException(description);
            case UNAVAILABLE -> new ServiceUnavailableException("Security service is unavailable");
            default -> new ServiceUnavailableException("Failed to " + operation + " user: " + description);
        };
    }
}
