package com.security_service.validator;

import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.exception.EmailAlreadyExistsException;
import com.security_service.exception.InvalidEmailFormatException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.exception.BadRequestException;
import com.security_service.exception.ConflictException;
import com.security_service.exception.NotFoundException;
import com.security_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository repository;

    public void validateEmailExists(String email) {
        if (repository.existsByEmail(email)) {
            throw new ConflictException("User with email=" + email + " already exists!");
        }
    }

    public void validateEmailNotExists(String email) {
        if (!repository.existsByEmail(email)) {
            throw new NotFoundException("User with email=" + email + " does not exist!");
        }
    }

    public void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BadRequestException("Email should be valid: \"mail@example.com\"");
        }
    }

    public void validateGuidExists(UUID guid) {
        if (!repository.existsByGuid(guid)) {
            throw new NotFoundException("User with guid=" + guid + " does not exist!");
        }
    }

    public void validateRegister(RegisterRequest request) {
        validateString(request.username(), "username");

        validateString(request.email(), "email");
        validateEmailFormat(request.email());
        validateEmailExists(request.email());

        validateString(request.password(), "password");
    }

    public void validateUpdate(SynchronizedUser synchronizedUser) {
        if (synchronizedUser.getUsername() != null) {
            validateString(synchronizedUser.getUsername(), "username");
        }

        if (synchronizedUser.getEmail() != null) {
            validateString(synchronizedUser.getEmail(), "email");
            validateEmailFormat(synchronizedUser.getEmail());
        }
    }
}
