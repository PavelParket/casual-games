package casualgames.userservice.service;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.dto.UserResponse;

import java.util.UUID;

public interface UserService extends Service<UserResponse, Long> {

    UserResponse create(UserRequest userRequest);

    UserResponse update(Long userId, UserRequest userRequest);

    UserResponse update(UUID guid, UserRequest request);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);
}
