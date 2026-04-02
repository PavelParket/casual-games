package com.security_service.service;

import com.casualgames.grpc.user.CreateUserRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.CustomUserDetails;
import com.security_service.domain.entity.User;
import com.security_service.exception.NotFoundException;
import com.security_service.exception.ServiceUnavailableException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.mapper.UserMapper;
import com.security_service.repository.UserRepository;
import com.security_service.service.grpc.client.GrpcUserClient;
import com.security_service.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_EMAIL;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_GUID;

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
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_EMAIL, email)));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByGuid(UUID guid) throws UsernameNotFoundException {
        User user = repository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid)));

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

    @Deprecated
    @Transactional
    public UserResponse updateById(Long id, UpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + id));

        validator.validateUpdate(request);

        mapper.updateEntity(user, request, passwordService);

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public UserResponse updateByGuid(UUID guid, UpdateRequest request) {
        User user = repository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid)));

        validator.validateUpdate(request);

        mapper.updateEntity(user, request, passwordService);

        return mapper.toResponse(repository.save(user));
    }

    @Deprecated
    @Transactional
    public void delete(Long id) {
        validator.validateIdExists(id);

        repository.deleteById(id);
    }

    @Transactional
    public void deleteByGuid(UUID guid) {
        validator.validateGuidExists(guid);

        repository.deleteByGuid(guid);
    }

    public List<UserResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Deprecated
    public UserResponse getById(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id=" + id)));
    }

    public UserResponse getByEmail(String email) {
        return mapper.toResponse(repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_EMAIL, email))));
    }

    public UserResponse getByGuid(UUID guid) {
        return mapper.toResponse(repository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid))));
    }
}
