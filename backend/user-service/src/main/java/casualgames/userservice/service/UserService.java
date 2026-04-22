package casualgames.userservice.service;

import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserSearchFilterRequest;
import casualgames.userservice.entity.User;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.grpc.client.GrpcSecurityClient;
import casualgames.userservice.service.helper.KafkaMessageHelper;
import casualgames.userservice.service.helper.PermissionHelper;
import casualgames.userservice.validator.UserValidator;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_DELETE_USER;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_READ_USER_BALANCE;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER_ROLE;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserValidator userValidator;

    private final GrpcSecurityClient grpcSecurityClient;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    private final KafkaMessageHelper kafkaMessageHelper;

    @Transactional
    public UserResponse update(UUID guid, UpdateUserRequest request) {
        PermissionContext context = permissionHelper.getContext(guid);
        AuthenticationToken token = permissionHelper.getToken();

        if (!permissionValidator.can(Permissions.USER, Operation.UPDATE, context, token)) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER);
        }

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, guid)));

        userValidator.validateEmailForUpdate(request.email(), target);

        permissionValidator.updateObject(target, request, context, token);

        User saved = userRepository.save(target);

        kafkaMessageHelper.save(kafkaMessageHelper.getTopics().getUser(), kafkaMessageHelper.buildMessage(saved));

        return buildResponse(saved, context, token);
    }

    public UserResponse buildResponse(User user, PermissionContext context, AuthenticationToken token) {
        UserResponse response = userMapper.toResponse(user);
        permissionValidator.readObject(response, context, token);
        return response;
    }

    @Transactional
    public void deleteByGuid(UUID guid) {
        if (!permissionValidator.can(Permissions.USER, Operation.DELETE, permissionHelper.getContext(guid), permissionHelper.getToken())) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_DELETE_USER);
        }

        if (!userRepository.existsByGuid(guid)) {
            throw new NotFoundException(String.format(NOT_FOUND_USER, guid));
        }

        userRepository.deleteByGuid(guid);

        grpcSecurityClient.delete(guid);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> buildResponse(
                        user,
                        permissionHelper.getContext(user.getGuid()),
                        permissionHelper.getToken()
                ))
                .toList();
    }

    public List<UserResponse> search(UserSearchFilterRequest request) {
        String status = request.status() == null ? null : request.status().name();
        return userRepository.search(request.username(), status).stream()
                .map(user -> buildResponse(user, permissionHelper.getContext(user.getGuid()), permissionHelper.getToken()))
                .toList();
    }

    public UserResponse findByGuid(UUID guid) {
        User user = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, guid)));

        return buildResponse(user, permissionHelper.getContext(user.getGuid()), permissionHelper.getToken());
    }

    @Transactional
    public UserResponse updateRole(UUID guid, Role role) {
        if (!permissionValidator.can(Permissions.ROLE, Operation.UPDATE, permissionHelper.getContext(guid), permissionHelper.getToken())) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER_ROLE);
        }

        User target = userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, guid)));

        target.setRole(role);

        User saved = userRepository.save(target);

        kafkaMessageHelper.save(kafkaMessageHelper.getTopics().getUser(), kafkaMessageHelper.buildMessage(saved));

        return buildResponse(saved, permissionHelper.getContext(saved.getGuid()), permissionHelper.getToken());
    }

    public BigDecimal getBalance(UUID guid) {
        if (!permissionValidator.can(Permissions.BALANCE, Operation.READ, permissionHelper.getContext(guid), permissionHelper.getToken())) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_READ_USER_BALANCE);
        }

        return userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, guid)))
                .getBalance();
    }
}
