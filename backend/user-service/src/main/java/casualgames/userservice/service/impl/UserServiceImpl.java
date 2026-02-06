package casualgames.userservice.service.impl;

import casualgames.userservice.client.SecurityServiceClient;
import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.dto.bank_service.TransactionShortInfoInternalRequest;
import casualgames.userservice.entity.User;
import casualgames.userservice.enums.TransactionStatus;
import casualgames.userservice.enums.TransactionType;
import casualgames.userservice.exception.ResourceNotFoundException;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.UserService;
import casualgames.userservice.validator.UserValidator;
import com.security_starter.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final SecurityServiceClient client;

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: '" + id + "' not found"));
    }

    @Override
    public List<UserResponse> findAll() {
        return userMapper.toListResponse(userRepository.findAll());
    }

    @Transactional
    @Override
    public UserResponseDto create(CreateUserRequest request) {

        userValidator.validateForCreation(request);

        User user = userMapper.toEntity(request);

        return userMapper.toDto(userRepository.save(user));
    }

    @Deprecated
    @Transactional
    @Override
    public UserResponse update(Long userId, UpdateUserRequest userRequest) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userValidator.validateEmailForUpdate(userRequest.email(), existingUser);

        userMapper.updateEntity(userRequest, existingUser);

        return userMapper.toResponseDto(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public UserResponseDto updateByGuid(UUID guid, UpdateUserRequest request) {
        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with guid: " + guid));

        userValidator.validateEmailForUpdate(request.email(), target);

        userMapper.updateEntity(request, target);

        User saved = userRepository.save(target);

        client.update(userMapper.toUpdateUserInternalRequest(saved, request.password()));

        return userMapper.toDto(saved);
    }

    @Deprecated
    @Transactional
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteByGuid(UUID guid) {
        if (!userRepository.existsByGuid(guid)) {
            throw new ResourceNotFoundException("User not found");
        }

        client.delete(guid);

        userRepository.deleteByGuid(guid);
    }

    @Override
    public List<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username).stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email + "' not found"));
    }

    @Override
    public UserResponseDto findByGuid(UUID guid) {
        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        return userMapper.toDto(target);
    }

    @Transactional
    public UserResponseDto updateRole(UUID guid, Role role) {
        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"));

        target.setRole(role);

        User saved = userRepository.save(target);

        /* todo: переделать потом, а то ничего не сработает
            вероятно пора добавлять outbox паттерн */
        //client.updateRole(actor, saved, role);

        return userMapper.toDto(saved);
    }

    @Override
    public BigDecimal getBalance(UUID guid) {
        return userRepository.findByGuid(guid)
                .orElseThrow(() -> new ResourceNotFoundException("User with guid: " + guid + "' not found"))
                .getBalance();
    }

    /* todo: пофиксить баг при котором на банк сервис возвращается null, а не boolean из-за чего транзакция
        с отрицательным балансом помечается как success, вместо reject
        Также есть проблема с тем что нормальная транзакция меняет баланс и он фиксируется в базе,
        а отрицательный - нет, возникает несогласованность */
    @Override
    @Transactional
    public Boolean updateBalances(List<TransactionShortInfoInternalRequest> transactions) {
        boolean allPending = transactions.stream()
                .allMatch(transaction -> TransactionStatus.PENDING.equals(transaction.status()));

        if (!allPending) {
            log.error("Not all transactions are PENDING");

            return false;
        }

        List<UUID> guids = transactions.stream()
                .map(TransactionShortInfoInternalRequest::userGuid)
                .distinct()
                .toList();

        List<User> users = userRepository.findAllByGuidWithLock(guids);

        if (users.size() != guids.size()) {
            log.error("Not all users found. Expected: {}, Found: {}", guids.size(), users.size());

            return false;
        }

        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        boolean allValid = transactions.stream()
                .allMatch(transaction -> {
                    User user = userMap.get(transaction.userGuid());
                    BigDecimal newBalance = TransactionType.ADDITION.equals(transaction.type())
                            ? user.getBalance().add(transaction.amount())
                            : user.getBalance().subtract(transaction.amount());

                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        log.error("Negative balance for user: {}", user.getGuid());

                        return false;
                    }

                    user.setBalance(newBalance);
                    return true;
                });

        if (!allValid) {
            return false;
        }

        userRepository.saveAll(users);

        log.info("Updated {} users with {} transactions", users.size(), transactions.size());

        return true;
    }
}
