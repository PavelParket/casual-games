package com.security_service.service.grpc;

import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.google.protobuf.Empty;
import com.security_service.exception.NotFoundException;
import com.security_service.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcSecurityService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> observer) {
        try {
            userService.delete(UUID.fromString(request.getGuid()));

            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (NotFoundException e) {
            log.warn("gRPC DeleteUser: user not found — {}", e.getMessage());
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC DeleteUser: unexpected error", e);
            observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
}
