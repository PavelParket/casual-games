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
import com.security_service.exception.NotFoundException;
import com.security_service.repository.UserPermissionRedisRepository;
import com.security_service.service.SyncPermissionService;
import com.security_starter.provider.PermissionProvider;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        try {
            Set<String> permissions = permissionProvider.loadPermissions(
                    new HashSet<>(request.getRolesList()),
                    request.getEmail()
            );

            observer.onNext(UserPermissionsResponse.newBuilder()
                    .addAllPermissions(permissions)
                    .build());
            observer.onCompleted();

        } catch (Exception e) {
            log.error("gRPC GetUserPermissions failed", e);
            observer.onError(Status.INTERNAL.withDescription("Failed to load permissions").asRuntimeException());
        }
    }

    @Override
    public void checkPermission(CheckPermissionRequest request,
                                StreamObserver<CheckPermissionResponse> observer) {
        try {
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

        } catch (Exception e) {
            log.error("gRPC CheckPermission failed", e);
            observer.onError(Status.INTERNAL.withDescription("Failed to check permission").asRuntimeException());
        }
    }

    @Override
    public void getRolePermissions(RolePermissionsRequest request,
                                   StreamObserver<RolePermissionsResponse> observer) {
        try {
            Set<String> permissions = redisRepository.getRolePermissions(request.getRoleName());

            observer.onNext(RolePermissionsResponse.newBuilder()
                    .addAllPermissions(permissions)
                    .build());
            observer.onCompleted();

        } catch (Exception e) {
            log.error("gRPC GetRolePermissions failed for role={}", request.getRoleName(), e);
            observer.onError(Status.INTERNAL.withDescription("Failed to get role permissions").asRuntimeException());
        }
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

        } catch (NotFoundException e) {
            log.warn("gRPC SyncUserPermissions: user not found — {}", e.getMessage());
            observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            log.warn("gRPC SyncUserPermissions: invalid guid — {}", e.getMessage());
            observer.onError(Status.INVALID_ARGUMENT.withDescription("Invalid user_guid format").asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC SyncUserPermissions failed", e);
            observer.onError(Status.INTERNAL.withDescription("Failed to sync permissions").asRuntimeException());
        }
    }

    private String buildPermission(String attribute, String operation, String scope) {
        return String.join(UNDERSCORE,
                attribute.toUpperCase(),
                operation.toUpperCase(),
                scope);
    }
}
