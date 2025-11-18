package com.security_service.factory;

/*@Component
@RequiredArgsConstructor
public class UserFactory implements Factory<User> {

    private final PasswordService passwordService;

    @Override
    public User create(Object... params) {
        String username = (String) params[0];
        String email = (String) params[1];
        String password = (String) params[2];
        Role role = params.length > 3 && params[3] != null ? (Role) params[3] : Role.USER;

        String encodedPassword = (password != null && !password.isBlank())
                ? passwordService.encode(password)
                : null;

        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .role(role)
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
}*/
