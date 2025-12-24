package casualgames.userservice.service.impl;

import casualgames.userservice.client.SecurityServiceClient;
import casualgames.userservice.config.PermissionContext;
import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.entity.User;
import casualgames.userservice.enums.Role;
import casualgames.userservice.exception.ForbiddenException;
import casualgames.userservice.exception.ResourceNotFoundException;
import casualgames.userservice.filter.UserSanitizer;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
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

    private final UserSanitizer userSanitizer;

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
        User user = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with guid: " + guid));

        userValidator.validateEmailForUpdate(request.email(), target);

        PermissionContext context = PermissionContext.builder()
                .isAdmin(Role.ADMIN.equals(user.getRole()))
                .isOwner(user.getGuid().equals(target.getGuid()))
                .build();

        if (!(context.canUpdateAnyProfile() || context.canUpdateOwnProfile())) {
            throw new ForbiddenException("You do not have permission to update this profile");
        }

        updateEntity(request, target);

        User saved = userRepository.save(target);

        client.update(userMapper.toUpdateUserInternalRequest(saved, request.password()));

        UserResponseDto response = userMapper.toDto(saved);

        userSanitizer.sanitize(response, context);

        return response;
    }

    private void updateEntity(UpdateUserRequest request, User user) {
        if (request.username() != null) {
            user.setUsername(request.username());
        }

        if (request.email() != null) {
            user.setEmail(request.email());
        }
    }

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
        User user = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        PermissionContext context = PermissionContext.builder()
                .isAdmin(Role.ADMIN.equals(user.getRole()))
                .isOwner(user.getGuid().equals(target.getGuid()))
                .build();

        UserResponseDto response = userMapper.toDto(target);

        userSanitizer.sanitize(response, context);

        return response;
    }

    private User getAuthUser() {
        return userRepository.findById(5L).orElse(null);
    }

    @Transactional
    public UserResponseDto updateRole(UUID guid, Role role) {
        User user = getAuthUser();

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        PermissionContext context = PermissionContext.builder()
                .isAdmin(Role.ADMIN.equals(user.getRole()))
                .isOwner(user.getGuid().equals(target.getGuid()))
                .build();

        if (!context.canUpdateRole()) {
            throw new ForbiddenException("You do not have permission to update role");
        }

        target.setRole(role);

        User saved = userRepository.save(target);

        // todo: переделать потом, а то ничего не сработает
        client.updateRole(user, saved, role);

        UserResponseDto response = userMapper.toDto(saved);

        userSanitizer.sanitize(response, context);

        return response;
    }
}
