package casualgames.userservice.service.grpc.client;

import casualgames.userservice.exception.ServiceUnavailableException;
import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcSecurityClient {

    @GrpcClient("security-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

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
