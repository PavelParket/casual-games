package com.security_service.service.grpc;

import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UpdateUserRequest;
import com.casualgames.grpc.user.UpdateUserResponse;
import com.casualgames.grpc.user.UpdateUserRoleRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.google.protobuf.Empty;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.exception.EmailAlreadyExistsException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

import static com.google.common.base.Strings.emptyToNull;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcSecurityService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> observer) {
        try {
            UpdateRequest updateRequest = UpdateRequest.builder()
                    .username(emptyToNull(request.getUsername()))
                    .email(emptyToNull(request.getEmail()))
                    .password(emptyToNull(request.getPassword()))
                    .build();

            UserResponse updateResponse = userService.updateByGuid(UUID.fromString(request.getGuid()), updateRequest);

            UpdateUserResponse response = UpdateUserResponse.newBuilder()
                    .setGuid(updateResponse.guid().toString())
                    .setUsername(updateResponse.username())
                    .setEmail(updateResponse.email())
                    .setRole(updateResponse.role())
                    .setCreatedAt(GrpcTimestampMapper.toTimestamp(updateResponse.createdAt()))
                    .build();

            observer.onNext(response);
            observer.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("gRPC UpdateUser: user not found — {}", e.getMessage());
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (EmailAlreadyExistsException e) {
            log.warn("gRPC UpdateUser: email conflict — {}", e.getMessage());
            observer.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC UpdateUser: unexpected error", e);
            observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void updateUserRole(UpdateUserRoleRequest request, StreamObserver<Empty> observer) {
        try {
            userService.updateRole(UUID.fromString(request.getGuid()), request.getRole());

            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("gRPC UpdateUserRole: user not found — {}", e.getMessage());
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC UpdateUserRole: unexpected error", e);
            observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> observer) {
        try {
            userService.deleteByGuid(UUID.fromString(request.getGuid()));

            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("gRPC DeleteUser: user not found — {}", e.getMessage());
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC DeleteUser: unexpected error", e);
            observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
}
