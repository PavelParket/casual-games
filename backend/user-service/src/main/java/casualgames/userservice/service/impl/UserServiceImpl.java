package casualgames.userservice.service.impl;

import casualgames.userservice.client.SecurityServiceClient;
import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.entity.User;
import casualgames.userservice.exception.ResourceNotFoundException;
import casualgames.userservice.exception.ServiceUnavailableException;
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
    public UserResponse updateByGuid(UUID guid, UpdateUserRequest request) {
        User user = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with guid: " + guid));

        userValidator.validateEmailForUpdate(request.email(), user);

        userMapper.updateEntity(request, user);

        try {
            client.update(userMapper.toUpdateUserInternalRequest(user, request.password()));
        } catch (ServiceUnavailableException e) {
            log.error("UserService is unavailable: {}", e.getMessage(), e);
            throw e;
        }

        return userMapper.toResponseDto(userRepository.save(user));
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
    public UserResponse findByGuid(UUID guid) {
        return userMapper.toResponseDto(userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found")));
    }
}
