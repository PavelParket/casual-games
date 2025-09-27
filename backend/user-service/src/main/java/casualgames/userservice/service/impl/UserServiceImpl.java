package casualgames.userservice.service.impl;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.entity.User;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void create(UserRequest userRequest) {

        if (userRepository.findByUsername(userRequest.username()).isPresent() ||
                userRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем или email уже существует");
        }

        User user = userMapper.toEntity(userRequest);
        userRepository.save(user);
    }

    @Override
    public void update(Long userId, UserRequest userRequest) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        validateUniqueness(
                userRequest.username(),
                userRepository::findByUsername,
                existingUser,
                "именем"
        );

        validateUniqueness(
                userRequest.email(),
                userRepository::findByEmail,
                existingUser,
                "email"
        );

        userMapper.toUpdateEntity(userRequest, existingUser);

        userRepository.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    private void validateUniqueness(
            String newValue,
            Function<String, Optional<User>> finder,
            User existingUser,
            String fieldName
    ) {
        if (newValue != null && !newValue.isBlank()) {
            finder.apply(newValue)
                    .filter(foundUser -> !foundUser.getId().equals(existingUser.getId()))
                    .ifPresent(foundUser -> {
                        throw new IllegalArgumentException(
                                "Пользователь с " + fieldName + " '" + newValue + "' уже существует"
                        );
                    });
        }
    }
}