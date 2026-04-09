package com.security_service.controller;

import com.security_service.domain.dto.admin.UserPermissionCreateRequest;
import com.security_service.domain.dto.admin.UserPermissionResponse;
import com.security_service.domain.dto.admin.UserPermissionUpdateRequest;
import com.security_service.service.UserPermissionService;
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
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping("/{userGuid}")
    public List<UserPermissionResponse> getByUserGuid(@PathVariable UUID userGuid) {
        return userPermissionService.getByUserGuid(userGuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserPermissionResponse create(@Valid @RequestBody UserPermissionCreateRequest request) {
        return userPermissionService.create(request);
    }

    @PutMapping("/{id}")
    public UserPermissionResponse update(@PathVariable Long id, @Valid @RequestBody UserPermissionUpdateRequest request) {
        return userPermissionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userPermissionService.delete(id);
    }

    @DeleteMapping("/user/{userGuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllByUserGuid(@PathVariable UUID userGuid) {
        userPermissionService.deleteAllByUserGuid(userGuid);
    }

    @PostMapping("/sync-redis")
    public void syncRedis() {
        userPermissionService.syncUserPermissionsToRedis();
    }
}
