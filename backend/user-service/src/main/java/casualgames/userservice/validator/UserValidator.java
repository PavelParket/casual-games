package casualgames.userservice.validator;

import casualgames.userservice.entity.User;
import casualgames.userservice.repository.UserRepository;
import com.common_utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_USER_EMAIL;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateForCreation(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException(String.format(CONFLICT_USER_EMAIL, user.getEmail()));
        }
    }

    public void validateEmailForUpdate(String newEmail, User existingUser) {
        if (newEmail != null) {
            userRepository.findByEmail(newEmail)
                    .filter(foundUser -> !foundUser.getId().equals(existingUser.getId()))
                    .ifPresent(foundUser -> {
                        throw new ConflictException(String.format(CONFLICT_USER_EMAIL, newEmail));
                    });
        }
    }
}