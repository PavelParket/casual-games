package casualgames.userservice.validator;

import casualgames.userservice.dto.CreateUserRequest;
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

    public void validateForCreation(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email '" + request.email() + "' already exists");
        }
    }

    @Deprecated(forRemoval = true)
    public void validateUsernameForUpdate(String newUsername, User existingUser) {
        if (newUsername != null) {

            Optional<User> foundUserOptional = userRepository.findByEmail(newUsername);

            foundUserOptional
                    .filter(foundUser -> !foundUser.getId().equals(existingUser.getId()))
                    .ifPresent(foundUser -> {
                        throw new ResourceAlreadyExistsException(
                                "User with name '" + newUsername + "' already exists"
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
                                "User with email '" + newEmail + "' already exists"
                        );
                    });
        }
    }
}