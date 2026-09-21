package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.grpc.user.CreateUserRequest;
import com.casualgames.kafkastarter.dto.event.sync.SynchronizedUser;
import com.casualgames.securityservice.domain.dto.RegisterRequest;
import com.casualgames.securityservice.domain.dto.UpdatePasswordRequest;
import com.casualgames.securityservice.domain.dto.UserResponse;
import com.casualgames.securityservice.domain.entity.CustomUserDetails;
import com.casualgames.securityservice.domain.entity.User;
import com.casualgames.securityservice.mapper.UserMapper;
import com.casualgames.securityservice.repository.UserRepository;
import com.casualgames.securityservice.service.grpc.client.GrpcUserClient;
import com.casualgames.securityservice.service.helper.PermissionHelper;
import com.casualgames.securityservice.validator.UserValidator;
import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.enums.Operation;
import com.casualgames.securitystarter.enums.Permissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.casualgames.securityservice.config.ResourceMessageConstants.FORBIDDEN_PASSWORD_UPDATE;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_EMAIL;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_GUID;

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
    public void updatePassword(UUID guid, UpdatePasswordRequest request, AuthenticationToken token) {
        User user = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        if (!permissionHelper.hasPermission(Permissions.PASSWORD, Operation.UPDATE, user.getGuid(), token)) {
            throw new ForbiddenException(FORBIDDEN_PASSWORD_UPDATE);
        }

        user.setPassword(passwordService.encode(request.newPassword()));

        userRepository.save(user);
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
