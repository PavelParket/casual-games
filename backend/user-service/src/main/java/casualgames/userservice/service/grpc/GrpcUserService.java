package casualgames.userservice.service.grpc;

import casualgames.userservice.domain.entity.User;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.validator.UserValidator;
import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.grpc.user.GetByGuidRequest;
import com.casualgames.grpc.user.UserResponse;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.common_utils.exception.NotFoundException;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;
import java.util.UUID;

import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    private final UserValidator userValidator;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        User newUser = userRepository.save(buildUser(request));

        responseObserver.onNext(buildUserResponse(newUser));
        responseObserver.onCompleted();
    }

    @Override
    public void getByGuid(GetByGuidRequest request, StreamObserver<UserResponse> responseObserver) {
        User user = userRepository.findByGuid(UUID.fromString(request.getGuid()))
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, request.getGuid())));

        responseObserver.onNext(buildUserResponse(user));
        responseObserver.onCompleted();
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
                .setLinkProfilePicture(Optional.ofNullable(user.getLinkProfilePicture()).orElse(""))
                .setLinkProfilePictureMini(Optional.ofNullable(user.getLinkProfilePictureMini()).orElse(""))
                .build();
    }
}
