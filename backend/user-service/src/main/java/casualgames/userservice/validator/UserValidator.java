package casualgames.userservice.validator;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.entity.User;
import casualgames.userservice.exception.ResourceAlreadyExistsException;
import casualgames.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateForCreation(UserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ResourceAlreadyExistsException("Пользователь с именем '" + request.username() + "' уже существует");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Пользователь с email '" + request.email() + "' уже существует");
        }
    }

    public void validateUsernameForUpdate(String newUsername, User existingUser) {
        if (newUsername != null) {

            Optional<User> foundUserOptional = userRepository.findByUsername(newUsername);

            foundUserOptional
                    .filter(foundUser -> !foundUser.getId().equals(existingUser.getId()))
                    .ifPresent(foundUser -> {
                        throw new ResourceAlreadyExistsException(
                                "Пользователь с именем '" + newUsername + "' уже существует"
                        );
                    });
        }
    }

    public void validateEmailForUpdate(String newEmail, User existingUser) {
        if (newEmail != null) {
            Optional<User> foundUserOptional = userRepository.findByEmail(newEmail);

            foundUserOptional
                    .filter(foundUser -> !foundUser.getId().equals(existingUser.getId()))
                    .ifPresent(foundUser -> {
                        throw new ResourceAlreadyExistsException(
                                "Пользователь с email '" + newEmail + "' уже существует"
                        );
                    });
        }
    }
}