package casualgames.userservice.service;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.entity.User;

import java.util.Optional;

public interface UserService extends Service<UserResponse, Long> {

    UserResponse create (UserRequest userRequest);

    UserResponse update (Long userId, UserRequest userRequest);

    Optional<UserResponse> findByUsername(String username);

    Optional<UserResponse> findByEmail(String email);
}
