package com.security_service.service;

import com.casualgames.grpc.user.CreateUserRequest;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdatePasswordRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.CustomUserDetails;
import com.security_service.domain.entity.User;
import com.security_service.mapper.UserMapper;
import com.security_service.repository.UserRepository;
import com.security_service.service.grpc.client.GrpcUserClient;
import com.security_service.service.helper.PermissionHelper;
import com.security_service.validator.UserValidator;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.FORBIDDEN_PASSWORD_UPDATE;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_EMAIL;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_GUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final PasswordService passwordService;

    private final GrpcUserClient grpcUserClient;

    private final PermissionHelper permissionHelper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(NOT_FOUND_USER_WITH_EMAIL, email)));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByGuid(UUID guid) throws UsernameNotFoundException {
        User user = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid)));

        return new CustomUserDetails(user);
    }

    @Transactional
    public UserResponse create(RegisterRequest request) {
        userValidator.validateRegister(request);

        User user = userMapper.toEntity(request, passwordService.encode(request.password()));

        grpcUserClient.create(buildCreateUserRequest(user));

        return userMapper.toResponse(userRepository.save(user));
    }

    private CreateUserRequest buildCreateUserRequest(User user) {
        return CreateUserRequest.newBuilder()
                .setGuid(user.getGuid().toString())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .build();
    }

    @Transactional
    public void synchronizeUpdatedUser(SynchronizedUser synchronizedUser) {
        User user = userRepository.findByGuid(synchronizedUser.getGuid())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        userValidator.validateUpdate(synchronizedUser);

        userMapper.updateEntity(user, synchronizedUser);

        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID guid) {
        userValidator.validateGuidExists(guid);

        userRepository.deleteByGuid(guid);
    }

    @Transactional
    public void updatePassword(UUID guid, UpdatePasswordRequest request) {
        User user = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        if (!permissionHelper.hasPermission(Permissions.PASSWORD, Operation.UPDATE, user.getGuid())) {
            throw new ForbiddenException(FORBIDDEN_PASSWORD_UPDATE);
        }

        user.setPassword(passwordService.encode(request.newPassword()));

        userRepository.save(user);

        log.info("Password changed for user guid={}", guid);
    }

    public List<UserResponse> getAll() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    public UserResponse getByEmail(String email) {
        return userMapper.toResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_EMAIL, email)))
        );
    }

    public UserResponse getByGuid(UUID guid) {
        return userMapper.toResponse(userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid)))
        );
    }
}
