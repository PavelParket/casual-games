package casualgames.userservice.service.impl;

import casualgames.userservice.client.SecurityServiceClient;
import casualgames.userservice.config.PermissionContext;
import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.entity.User;
import casualgames.userservice.enums.Operation;
import casualgames.userservice.enums.Permissions;
import casualgames.userservice.enums.Role;
import casualgames.userservice.exception.ForbiddenException;
import casualgames.userservice.exception.ResourceNotFoundException;
import casualgames.userservice.factory.PermissionContextFactory;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
import casualgames.userservice.validator.PermissionValidator;
import casualgames.userservice.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final SecurityServiceClient client;

    private final PermissionValidator permissionValidator;

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: '" + id + "' not found"));
    }

    @Override
    public List<UserResponse> findAll() {
        return userMapper.toListResponse(userRepository.findAll());
    }

    @Transactional
    @Override
    public UserResponse create(CreateUserRequest request) {

        userValidator.validateForCreation(request);

        User user = userMapper.toEntity(request);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Deprecated
    @Transactional
    @Override
    public UserResponse update(Long userId, UpdateUserRequest userRequest) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userValidator.validateEmailForUpdate(userRequest.email(), existingUser);

        userMapper.updateEntity(userRequest, existingUser);

        return userMapper.toResponseDto(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public UserResponseDto updateByGuid(UUID guid, UpdateUserRequest request) {
        User actor = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with guid: " + guid));

        userValidator.validateEmailForUpdate(request.email(), target);

        PermissionContext context = PermissionContextFactory.create(
                actor.getRole(),
                actor.getGuid().equals(target.getGuid()),
                actor.getGuid(),
                target.getGuid()
        );

        permissionValidator.updateObject(request, target, context);

        User saved = userRepository.save(target);

        //client.update(userMapper.toUpdateUserInternalRequest(saved, request.password()));

        UserResponseDto response = userMapper.toDto(saved);

        permissionValidator.readObject(response, context);

        return response;
    }

    @Deprecated
    @Transactional
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteByGuid(UUID guid) {
        if (!userRepository.existsByGuid(guid)) {
            throw new ResourceNotFoundException("User not found");
        }

        client.delete(guid);

        userRepository.deleteByGuid(guid);
    }

    @Override
    public List<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username).stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email + "' not found"));
    }

    @Override
    public UserResponseDto findByGuid(UUID guid) {
        User actor = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        PermissionContext context = PermissionContextFactory.create(
                actor.getRole(),
                actor.getGuid().equals(target.getGuid()),
                actor.getGuid(),
                target.getGuid()
        );

        UserResponseDto response = userMapper.toDto(target);

        permissionValidator.readObject(response, context);

        return response;
    }

    private User getAuthUser() {
        return userRepository.findById(3L).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserResponseDto updateRole(UUID guid, Role role) {
        User actor = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        PermissionContext context = PermissionContextFactory.create(
                actor.getRole(),
                actor.getGuid().equals(target.getGuid()),
                actor.getGuid(),
                target.getGuid()
        );

        if (!permissionValidator.can(Permissions.ROLE, Operation.UPDATE, context)) {
            throw new ForbiddenException("You do not have permission to update role");
        }

        target.setRole(role);

        User saved = userRepository.save(target);

        // todo: переделать потом, а то ничего не сработает
        // todo: вероятно пора добавлять outbox паттерн
        //client.updateRole(actor, saved, role);

        UserResponseDto response = userMapper.toDto(saved);

        permissionValidator.readObject(response, context);

        return response;
    }
}
