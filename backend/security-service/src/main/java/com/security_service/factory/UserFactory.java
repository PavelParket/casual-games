package com.security_service.factory;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.entity.User;
import com.security_service.domain.enums.Role;
import com.security_service.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordService passwordService;

    private User create(String username, String email, String password, Role role) {
        String encodedPassword = (password != null && !password.isBlank())
                ? passwordService.encode(password)
                : null;

        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .role(role != null ? role : Role.USER)
                .createdAt(Instant.now())
                .build();
    }

    public User createFromRegisterRequest(RegisterRequest request) {
        return create(request.username(), request.email(), request.password(), Role.USER);
    }

    @Deprecated
    public User createFromParams(String username, String email, String password, Role role) {
        return create(username, email, password, role);
    }
}
