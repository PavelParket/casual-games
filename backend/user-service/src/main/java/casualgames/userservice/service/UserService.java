package casualgames.userservice.service;

import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.bank_service.TransactionShortInfoInternalRequest;

import java.util.List;
import java.util.UUID;

public interface UserService extends Service<UserResponse, Long> {

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long userId, UpdateUserRequest request);

    UserResponse updateByGuid(UUID guid, UpdateUserRequest request);

    List<UserResponse> findByUsername(String username);

    UserResponse findByEmail(String email);

    UserResponse findByGuid(UUID guid);

    void deleteByGuid(UUID id);

    Boolean updateBalances(List<TransactionShortInfoInternalRequest> transactions);
}
