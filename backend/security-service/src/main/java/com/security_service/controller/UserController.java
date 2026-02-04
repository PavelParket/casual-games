package com.security_service.controller;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody RegisterRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/id={id}")
    public UserResponse updateById(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        return service.updateById(id, request);
    }

    @PutMapping("/guid={guid}")
    public UserResponse updateByGuid(@PathVariable UUID guid, @Valid @RequestBody UpdateRequest request) {
        return service.updateByGuid(guid, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/id={id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping("/guid={guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByGuid(@PathVariable UUID guid) {
        service.deleteByGuid(guid);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id")
    public UserResponse getById(@RequestParam Long id) {
        return service.getById(id);
    }

    @GetMapping("/email")
    public UserResponse getByEmail(@RequestParam String email) {
        return service.getByEmail(email);
    }
}
