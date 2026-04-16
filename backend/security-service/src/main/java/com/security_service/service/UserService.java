package com.security_service.service;

import com.casualgames.grpc.user.CreateUserRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.CustomUserDetails;
import com.security_service.domain.entity.User;
import com.security_service.exception.ServiceUnavailableException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.mapper.UserMapper;
import com.security_service.repository.UserRepository;
import com.security_service.service.grpc.client.GrpcUserClient;
import com.security_service.validator.UserValidator;
import com.security_starter.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    private final UserMapper mapper;

    private final UserValidator validator;

    private final PasswordService passwordService;

    private final GrpcUserClient grpcUserClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + email));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByGuid(UUID guid) throws UsernameNotFoundException {
        User user = repository.findByGuid(guid)
                .orElseThrow(() -> new UserNotFoundException("User not found with guid=" + guid));

        return new CustomUserDetails(user);
    }

    @Transactional
    public UserResponse create(RegisterRequest request) {
        validator.validateRegister(request);

        User user = mapper.toEntity(request, passwordService);

        try {
            grpcUserClient.create(buildCreateUserRequest(user));
        } catch (ServiceUnavailableException e) {
            log.error("UserService is unavailable: {}", e.getMessage());
            throw e;
        }

        return mapper.toResponse(repository.save(user));
    }

    private CreateUserRequest buildCreateUserRequest(User user) {
        return CreateUserRequest.newBuilder()
                .setGuid(user.getGuid().toString())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .build();
    }

    // todo: перевести на асинхронное обновление через кафку
    @Transactional
    public UserResponse update(UUID guid, UpdateRequest request) {
        User user = repository.findByGuid(guid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validator.validateUpdate(request);

        mapper.updateEntity(user, request, passwordService);

        return mapper.toResponse(repository.save(user));
    }

    // todo: перевести на асинхронное обновление через кафку
    @Transactional
    public void updateRole(UUID guid, String roleName) {
        User user = repository.findByGuid(guid)
                .orElseThrow(() -> new UserNotFoundException("User not found with guid=" + guid));

        validator.validateRoleExists(roleName);
        Role newRole = Role.valueOf(roleName);

        if (!user.getRole().equals(newRole)) {
            user.setRole(newRole);
            repository.save(user);

            log.info("Role changed to {} for user {}.", newRole, guid);
        }
    }

    @Transactional
    public void delete(UUID guid) {
        validator.validateGuidExists(guid);

        repository.deleteByGuid(guid);
    }

    public List<UserResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    public UserResponse getByEmail(String email) {
        return mapper.toResponse(repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + email)));
    }

    public UserResponse getByGuid(UUID guid) {
        return mapper.toResponse(repository.findByGuid(guid)
                .orElseThrow(() -> new UserNotFoundException("User not found with guid=" + guid)));
    }
}
