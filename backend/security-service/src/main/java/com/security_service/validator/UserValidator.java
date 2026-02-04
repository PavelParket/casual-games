package com.security_service.validator;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.exception.EmailAlreadyExistsException;
import com.security_service.exception.InvalidEmailFormatException;
import com.security_service.exception.InvalidRoleException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.repository.UserRepository;
import com.security_starter.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository repository;

    public void validateEmailExists(String email) {
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("User with email=" + email + " already exists!");
        }
    }

    public void validateEmailNotExists(String email) {
        if (!repository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email=" + email + " does not exist!");
        }
    }

    public void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidEmailFormatException("Email should be valid: \"mail@example.com\"");
        }
    }

    public void validateRoleExists(String roleName) {
        if (Arrays.stream(Role.values())
                .noneMatch(role -> role.name().equals(roleName))) {
            throw new InvalidRoleException("Role " + roleName + " not found!");
        }
    }

    public void validateIdExists(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User with id=" + id + " does not exist!");
        }
    }

    public void validateGuidExists(UUID guid) {
        if (!repository.existsByGuid(guid)) {
            throw new UserNotFoundException("User with guid=" + guid + " does not exist!");
        }
    }

    public void validateRegister(RegisterRequest request) {
        validateString(request.username(), "username");

        validateString(request.email(), "email");
        validateEmailFormat(request.email());
        validateEmailExists(request.email());

        validateString(request.password(), "password");
    }

    public void validateUpdate(UpdateRequest request) {
        if (request.username() != null) {
            validateString(request.username(), "username");
        }

        if (request.email() != null) {
            validateString(request.email(), "email");
            validateEmailFormat(request.email());
        }

        if (request.password() != null) {
            validateString(request.password(), "password");
        }

        if (request.role() != null) {
            validateString(request.role(), "role");
            validateRoleExists(request.role());
        }
    }
}
