package casualgames.userservice.service;

import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.dto.bank_service.TransactionShortInfoInternalRequest;
import com.security_starter.enums.Role;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface UserService extends Service<UserResponse, Long> {

    UserResponseDto create(CreateUserRequest request);

    UserResponse update(Long userId, UpdateUserRequest request);

    UserResponseDto updateByGuid(UUID guid, UpdateUserRequest request);

    List<UserResponse> findByUsername(String username);

    UserResponse findByEmail(String email);

    UserResponseDto findByGuid(UUID guid);

    void deleteByGuid(UUID id);

    Boolean updateBalances(List<TransactionShortInfoInternalRequest> transactions);

    UserResponseDto updateRole(UUID guid, Role role);

    BigDecimal getBalance(UUID guid);
}
