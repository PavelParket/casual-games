package com.security_service.controller;

import com.common_utils.dto.ErrorResponse;
import com.security_service.domain.dto.admin.RolePermissionResponse;
import com.security_service.domain.dto.admin.RoleResponse;
import com.security_service.service.RolePermissionService;
import com.security_service.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
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
public class RoleController {

    private final RoleService roleService;

    private final RolePermissionService rolePermissionService;

    @GetMapping
    @Operation(summary = "Get list of all roles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<RoleResponse> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role details by id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @GetMapping("/permissions/{roleId}")
    @Operation(summary = "Get specific role mapped with its permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public RolePermissionResponse getRoleWithPermissions(@PathVariable Long roleId) {
        return rolePermissionService.getRoleWithPermissions(roleId);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get all roles mapped with their permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<RolePermissionResponse> getAllRolesWithPermissions() {
        return rolePermissionService.getAllRolesWithPermissions();
    }
}
