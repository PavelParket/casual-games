package casualgames.userservice.service;

import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserSearchFilterRequest;
import com.security_starter.enums.Role;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface UserService extends Service<UserResponse, Long> {

    UserResponse update(UUID userId, UpdateUserRequest request);

    List<UserResponse> search(UserSearchFilterRequest request);

    UserResponse findByGuid(UUID guid);

    void deleteByGuid(UUID id);

    UserResponse updateRole(UUID guid, Role role);

    BigDecimal getBalance(UUID guid);
}
