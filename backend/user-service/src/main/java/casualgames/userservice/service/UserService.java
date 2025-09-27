package casualgames.userservice.service;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.entity.User;

import java.util.Optional;

public interface UserService extends Service<User, Long> {

    void create (UserRequest userRequest);

    void update (Long userId, UserRequest userRequest);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
