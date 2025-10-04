package casualgames.userservice.service.impl;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.entity.User;
import casualgames.userservice.exception.ResourceNotFoundException;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
import casualgames.userservice.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final UserValidator  userValidator;

    @Override
    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto);
    }

    @Override
    public List<UserResponse> findAll() {
        return userMapper.toListResponse(userRepository.findAll());
    }

    @Transactional
    @Override
    public UserResponse create(UserRequest userRequest) {

        userValidator.validateForCreation(userRequest);

        User user = userMapper.toEntity(userRequest);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse update(Long userId, UserRequest userRequest) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        userValidator.validateUsernameForUpdate(userRequest.username(), existingUser);

        userValidator.validateEmailForUpdate(userRequest.email(), existingUser);

        userMapper.toUpdateEntity(userRequest, existingUser);

        return userMapper.toResponseDto(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto);
    }
}