package casualgames.userservice.service.grpc.client;

import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.common_utils.exception.GrpcStatusExceptionMapper;
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

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public void delete(UUID guid) {
        DeleteUserRequest grpcRequest = DeleteUserRequest.newBuilder()
                .setGuid(guid.toString())
                .build();

        try {
            userServiceBlockingStub.deleteUser(grpcRequest);
        } catch (StatusRuntimeException e) {
            log.error("gRPC DeleteUser failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw grpcStatusExceptionMapper.toException(e);
        }
    }
}
