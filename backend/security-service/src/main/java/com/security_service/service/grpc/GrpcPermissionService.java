package com.security_service.service.grpc;

import com.casualgames.grpc.permission.CheckPermissionRequest;
import com.casualgames.grpc.permission.CheckPermissionResponse;
import com.casualgames.grpc.permission.PermissionServiceGrpc;
import com.casualgames.grpc.permission.RolePermissionsRequest;
import com.casualgames.grpc.permission.RolePermissionsResponse;
import com.casualgames.grpc.permission.SyncUserPermissionsRequest;
import com.casualgames.grpc.permission.SyncUserPermissionsResponse;
import com.casualgames.grpc.permission.UserPermissionsRequest;
import com.casualgames.grpc.permission.UserPermissionsResponse;
import com.common_utils.exception.BadRequestException;
import com.security_service.repository.UserPermissionRedisRepository;
import com.security_service.service.SyncPermissionService;
import com.security_starter.provider.PermissionProvider;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.BAD_REQUEST_INVALID_GUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcPermissionService extends PermissionServiceGrpc.PermissionServiceImplBase {

    private static final String UNDERSCORE = "_";
    private static final String FOR_ALL = "FOR_ALL";
    private static final String FOR_ME = "FOR_ME";
    private static final String WITHOUT_ME = "WITHOUT_ME";

    private final PermissionProvider permissionProvider;
    private final UserPermissionRedisRepository redisRepository;
    private final SyncPermissionService syncPermissionService;

    @Override
    public void getUserPermissions(UserPermissionsRequest request,
                                   StreamObserver<UserPermissionsResponse> observer) {
        Set<String> permissions = permissionProvider.loadPermissions(
                new HashSet<>(request.getRolesList()),
                request.getEmail()
        );

        observer.onNext(UserPermissionsResponse.newBuilder()
                .addAllPermissions(permissions)
                .build());
        observer.onCompleted();
    }

    @Override
    public void checkPermission(CheckPermissionRequest request,
                                StreamObserver<CheckPermissionResponse> observer) {
        Set<String> permissions = permissionProvider.loadPermissions(
                new HashSet<>(request.getRolesList()),
                request.getEmail()
        );

        boolean isOwner = request.getIsOwner();
        String forAll = buildPermission(request.getAttribute(), request.getOperation(), FOR_ALL);
        String forMe = buildPermission(request.getAttribute(), request.getOperation(), FOR_ME);
        String withoutMe = buildPermission(request.getAttribute(), request.getOperation(), WITHOUT_ME);

        boolean allowed = false;
        String matched = "";

        if (permissions.contains(forAll)) {
            allowed = true;
            matched = forAll;
        } else if (permissions.contains(forMe) && isOwner) {
            allowed = true;
            matched = forMe;
        } else if (permissions.contains(withoutMe) && !isOwner) {
            allowed = true;
            matched = withoutMe;
        }

        observer.onNext(CheckPermissionResponse.newBuilder()
                .setAllowed(allowed)
                .setMatchedPermission(matched)
                .build());
        observer.onCompleted();
    }

    @Override
    public void getRolePermissions(RolePermissionsRequest request,
                                   StreamObserver<RolePermissionsResponse> observer) {
        Set<String> permissions = redisRepository.getRolePermissions(request.getRoleName());

        observer.onNext(RolePermissionsResponse.newBuilder()
                .addAllPermissions(permissions)
                .build());
        observer.onCompleted();
    }

    @Override
    public void syncUserPermissions(SyncUserPermissionsRequest request,
                                    StreamObserver<SyncUserPermissionsResponse> observer) {
        try {
            UUID userGuid = UUID.fromString(request.getUserGuid());

            syncPermissionService.syncUserPermissions(userGuid);

            observer.onNext(SyncUserPermissionsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Permissions synced for user: " + userGuid)
                    .build());

            observer.onCompleted();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format(BAD_REQUEST_INVALID_GUID, request.getUserGuid()));
        }
    }

    private String buildPermission(String attribute, String operation, String scope) {
        return String.join(UNDERSCORE,
                attribute.toUpperCase(),
                operation.toUpperCase(),
                scope);
    }
}
