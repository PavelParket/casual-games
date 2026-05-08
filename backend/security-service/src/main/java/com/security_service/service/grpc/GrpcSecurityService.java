package com.security_service.service.grpc;

import com.casualgames.grpc.user.DeleteUserRequest;
import com.casualgames.grpc.user.UserServiceGrpc;
import com.google.protobuf.Empty;
import com.security_service.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

//todo: переименовать заменив security может на auth или user
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcSecurityService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> observer) {
        userService.delete(UUID.fromString(request.getGuid()));

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }
}
