package com.casualgames.securityservice.validator;

import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ConflictException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.kafkastarter.dto.event.sync.SynchronizedUser;
import com.casualgames.securityservice.domain.dto.RegisterRequest;
import com.casualgames.securityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.casualgames.securityservice.config.ResourceMessageConstants.BAD_REQUEST_EMAIL_FORMAT;
import static com.casualgames.securityservice.config.ResourceMessageConstants.CONFLICT_USER_EMAIL;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_EMAIL;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER_WITH_GUID;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository repository;

    public void validateEmailExists(String email) {
        if (repository.existsByEmail(email)) {
            throw new ConflictException(String.format(CONFLICT_USER_EMAIL, email));
        }
    }

    public void validateEmailNotExists(String email) {
        if (!repository.existsByEmail(email)) {
            throw new NotFoundException(String.format(NOT_FOUND_USER_WITH_EMAIL, email));
        }
    }

    public void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BadRequestException(BAD_REQUEST_EMAIL_FORMAT);
        }
    }

    public void validateGuidExists(UUID guid) {
        if (!repository.existsByGuid(guid)) {
            throw new NotFoundException(String.format(NOT_FOUND_USER_WITH_GUID, guid));
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
