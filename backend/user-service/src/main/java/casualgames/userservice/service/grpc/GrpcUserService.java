package casualgames.userservice.service.grpc;

import casualgames.userservice.entity.User;
import casualgames.userservice.exception.InvalidInputException;
import casualgames.userservice.exception.ResourceAlreadyExistsException;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
import casualgames.userservice.validator.UserValidator;
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

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    @Override
    public void createUser(CreateUserRequest createUserRequest, StreamObserver<UserResponse> responseObserver) {
        try {
            User newUser = userRepository.save(buildUser(createUserRequest));

            responseObserver.onNext(buildUserResponse(newUser));
            responseObserver.onCompleted();
        } catch (ResourceAlreadyExistsException e) {
            log.warn("gRPC CreateUser: resource already exists — {}", e.getMessage());
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (InvalidInputException e) {
            log.warn("gRPC CreateUser: invalid input — {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC CreateUser: unexpected error", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    private User buildUser(CreateUserRequest createUserRequest) {
        User user = User.builder()
                .guid(UUID.fromString(createUserRequest.getGuid()))
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .build();

        userValidator.validateForCreation(user);

        return user;
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.newBuilder()
                .setGuid(user.getGuid().toString())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setBalance(user.getBalance().toPlainString())
                .setRole(user.getRole().toString())
                .setStatus(user.getStatus().toString())
                .setCreatedAt(GrpcTimestampMapper.toTimestamp(user.getCreatedAt()))
                .build();
    }
}
