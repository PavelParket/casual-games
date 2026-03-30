package casualgames.userservice.service.grpc;

import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.exception.InvalidInputException;
import casualgames.userservice.exception.ResourceAlreadyExistsException;
import casualgames.userservice.service.UserService;
import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.UserGrpc;
import com.casualgames.grpc.user.UserResponse;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcUserService extends UserGrpc.UserImplBase {

    private final UserService userService;

    // todo: переделать, подумать, чтобы полностью перенести процесс создания в этот класс
    @Override
    public void createUser(CreateUserRequest createUserRequest, StreamObserver<UserResponse> observer) {
        try {
            casualgames.userservice.dto.CreateUserRequest request = casualgames.userservice.dto.CreateUserRequest.builder()
                    .guid(UUID.fromString(createUserRequest.getGuid()))
                    .username(createUserRequest.getUsername())
                    .email(createUserRequest.getEmail())
                    .build();

            UserResponseDto responseDto = userService.create(request);

            UserResponse response = UserResponse.newBuilder()
                    .setId(responseDto.getId())
                    .setGuid(responseDto.getGuid().toString())
                    .setUsername(responseDto.getUsername())
                    .setEmail(responseDto.getEmail())
                    .setBalance(responseDto.getBalance().toPlainString())
                    .setRole(responseDto.getRole())
                    .setStatus(responseDto.getStatus())
                    .setCreatedAt(GrpcTimestampMapper.toTimestamp(responseDto.getCreatedAt()))
                    .build();

            observer.onNext(response);
            observer.onCompleted();
        } catch (ResourceAlreadyExistsException e) {
            log.warn("gRPC CreateUser: resource already exists — {}", e.getMessage());
            observer.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (InvalidInputException e) {
            log.warn("gRPC CreateUser: invalid input — {}", e.getMessage());
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC CreateUser: unexpected error", e);
            observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
}
