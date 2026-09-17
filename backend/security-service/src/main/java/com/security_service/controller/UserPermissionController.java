package com.security_service.controller;

import com.common_utils.dto.ErrorResponse;
import com.security_service.domain.dto.admin.UserPermissionCreateRequest;
import com.security_service.domain.dto.admin.UserPermissionResponse;
import com.security_service.domain.dto.admin.UserPermissionUpdateRequest;
import com.security_service.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/personal-permissions")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
@ApiResponses(value = {
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping("/{userGuid}")
    @Operation(summary = "Get specific user permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<UserPermissionResponse> getByUserGuid(@PathVariable UUID userGuid) {
        return userPermissionService.getByUserGuid(userGuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant a permission to user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201")
    public UserPermissionResponse create(@Valid @RequestBody UserPermissionCreateRequest request) {
        return userPermissionService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update granted user permission mapping", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public UserPermissionResponse update(@PathVariable Long id, @Valid @RequestBody UserPermissionUpdateRequest request) {
        return userPermissionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke specific permission mapping", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204")
    public void delete(@PathVariable Long id) {
        userPermissionService.delete(id);
    }

    @DeleteMapping("/user/{userGuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke all permissions from a user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204")
    public void deleteAllByUserGuid(@PathVariable UUID userGuid) {
        userPermissionService.deleteAllByUserGuid(userGuid);
    }

    @Deprecated
    @PostMapping("/sync-redis")
    @Operation(summary = "Force synchronization of user permissions to Redis cache", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public void syncRedis() {
        userPermissionService.syncUserPermissionsToRedis();
    }
}
