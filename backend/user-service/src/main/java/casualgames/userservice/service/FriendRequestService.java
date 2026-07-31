package casualgames.userservice.service;

import casualgames.userservice.domain.dto.FriendRequestRequest;
import casualgames.userservice.domain.dto.FriendRequestResponse;
import casualgames.userservice.domain.dto.FriendRequestResponseList;
import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import casualgames.userservice.mapper.FriendMapper;
import casualgames.userservice.mapper.FriendRequestMapper;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.FriendRequestRepository;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.helper.PermissionHelper;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static casualgames.userservice.config.ResourceMessageConstants.BAD_REQUEST_SELF_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_ALREADY_FRIENDS;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_COOLDOWN;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_LIMIT_EXCEEDED;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {

    private static final int PENDING_REQUEST_LIMIT = 15;
    private static final int REQUEST_COOLDOWN_DAYS = 1;

    private final FriendRequestRepository friendRequestRepository;

    private final FriendshipRepository friendshipRepository;

    private final UserRepository userRepository;

    private final FriendRequestMapper friendRequestMapper;

    private final FriendMapper friendMapper;

    private final UserMapper userMapper;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    @Transactional
    public FriendRequestResponse create(UUID recipientGuid, AuthenticationToken authenticationToken) {
        User requester = userRepository.findByGuid(authenticationToken.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, authenticationToken.getGuid())));

        if (!permissionValidator.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                permissionHelper.getContext(requester.getGuid(), authenticationToken),
                authenticationToken
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        if (requester.getGuid().equals(recipientGuid)) {
            throw new BadRequestException(BAD_REQUEST_SELF_FRIEND_REQUEST);
        }

        User recipient = userRepository.findByGuid(recipientGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, recipientGuid)));

        if (friendshipRepository.existsByUserGuidAndFriendGuid(requester.getGuid(), recipient.getGuid())) {
            throw new ConflictException(CONFLICT_ALREADY_FRIENDS);
        }

        Instant liveSince = Instant.now().minus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS);

        if (friendRequestRepository.existsCooldown(requester.getGuid(), recipient.getGuid(), liveSince)) {
            throw new ConflictException(CONFLICT_REQUEST_COOLDOWN);
        }

        if (friendRequestRepository.countByRequesterGuidAndStatusAndCreatedAtAfter(requester.getGuid(), FriendRequestStatus.PENDING, liveSince) >= PENDING_REQUEST_LIMIT) {
            throw new ConflictException(CONFLICT_REQUEST_LIMIT_EXCEEDED);
        }

        Optional<FriendRequest> existingMirrorRequest = friendRequestRepository.findByRequesterGuidAndRecipientGuidAndStatus(
                        recipient.getGuid(),
                        requester.getGuid(),
                        FriendRequestStatus.PENDING
                )
                .filter(friendRequest -> friendRequest.getCreatedAt().isAfter(liveSince));

        if (existingMirrorRequest.isPresent()) {
            return buildFriendship(requester, recipient, List.of(existingMirrorRequest.get()));
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository.findByRequesterGuidAndRecipientGuidAndStatus(
                requester.getGuid(),
                recipient.getGuid(),
                FriendRequestStatus.PENDING
        );

        if (existingRequest.isPresent()) {
            if (existingRequest.get().getCreatedAt().isAfter(liveSince)) {
                return friendRequestMapper.toResponse(
                        existingRequest.get(),
                        userMapper.toResponse(requester),
                        userMapper.toResponse(recipient)
                );
            }

            log.info("Replacing stale pending request from user {} -> to user {}", requester.getGuid(), recipient.getGuid());
            friendRequestRepository.delete(existingRequest.get());
            friendRequestRepository.flush();
        }

        FriendRequest newRequest = friendRequestRepository.save(
                FriendRequest.builder()
                        .requesterGuid(requester.getGuid())
                        .recipientGuid(recipient.getGuid())
                        .build()
        );

        return friendRequestMapper.toResponse(
                friendRequestRepository.save(newRequest),
                userMapper.toResponse(requester),
                userMapper.toResponse(recipient)
        );
    }

    @Transactional
    public FriendRequestResponse update(FriendRequestRequest friendRequestRequest, AuthenticationToken authenticationToken) {
        switch (friendRequestRequest.status()) {
            case ACCEPTED -> {
                return processAccept(friendRequestRequest, authenticationToken);
            }
            case DECLINED, CANCELED -> {
                return processDeclineOrCancel(friendRequestRequest, authenticationToken);
            }
            default -> throw new BadRequestException("Invalid friend request status");
        }
    }

    @Transactional
    public FriendRequestResponse processAccept(FriendRequestRequest friendRequestRequest, AuthenticationToken authenticationToken) {
        User requester = userRepository.findByGuid(authenticationToken.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, authenticationToken.getGuid())));

        if (!permissionValidator.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                permissionHelper.getContext(requester.getGuid(), authenticationToken),
                authenticationToken
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        FriendRequest request = friendRequestRepository.findById(friendRequestRequest.id())
                .filter(friendRequest -> friendRequest.getRecipientGuid().equals(requester.getGuid()))
                .filter(friendRequest -> FriendRequestStatus.PENDING.equals(friendRequest.getStatus()))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIEND_REQUEST));

        User recipient = userRepository.findByGuid(request.getRecipientGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, request.getRecipientGuid())));

        return buildFriendship(requester, recipient, List.of(request));
    }

    private FriendRequestResponse buildFriendship(User requester, User recipient, List<FriendRequest> requestsToDelete) {
        List<Friendship> friendships = List.of(
                Friendship.builder()
                        .userGuid(requester.getGuid())
                        .friendGuid(recipient.getGuid())
                        .build(),
                Friendship.builder()
                        .userGuid(recipient.getGuid())
                        .friendGuid(requester.getGuid())
                        .build()
        );

        try {
            friendshipRepository.saveAll(friendships);
        } catch (DataIntegrityViolationException e) {
            log.info("Friendship {} <-> {} already exists, treating as idempotent success", requester.getGuid(), recipient.getGuid());
        }

        if (!requestsToDelete.isEmpty()) {
            friendRequestRepository.deleteAllById(
                    requestsToDelete.stream()
                            .map(FriendRequest::getId)
                            .toList()
            );
        }

        FriendRequest friendRequest = requestsToDelete.getFirst();

        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);

        return friendRequestMapper.toResponse(
                friendRequest,
                userMapper.toResponse(requester),
                userMapper.toResponse(recipient)
        );
    }

    @Transactional
    public FriendRequestResponse processDeclineOrCancel(FriendRequestRequest friendRequestRequest, AuthenticationToken authenticationToken) {
        User requester = userRepository.findByGuid(authenticationToken.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, authenticationToken.getGuid())));

        if (!permissionValidator.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                permissionHelper.getContext(requester.getGuid(), authenticationToken),
                authenticationToken
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        FriendRequest request = friendRequestRepository.findById(friendRequestRequest.id())
                .filter(friendRequest -> friendRequest.getRecipientGuid().equals(requester.getGuid()))
                .filter(friendRequest -> FriendRequestStatus.PENDING.equals(friendRequest.getStatus()))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIEND_REQUEST));

        User recipient = userRepository.findByGuid(request.getRecipientGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, request.getRecipientGuid())));

        request.setStatus(friendRequestRequest.status());
        request.setResolvedAt(Instant.now());

        return friendRequestMapper.toResponse(
                friendRequestRepository.save(request),
                userMapper.toResponse(requester),
                userMapper.toResponse(recipient)
        );
    }

    @Transactional(readOnly = true)
    public FriendRequestResponseList getIncoming(Pageable pageable, AuthenticationToken authenticationToken) {
        User requester = userRepository.findByGuid(authenticationToken.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, authenticationToken.getGuid())));

        if (!permissionValidator.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.READ,
                permissionHelper.getContext(requester.getGuid(), authenticationToken),
                authenticationToken
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        Instant liveSince = Instant.now().minus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS);

        Page<FriendRequest> friendRequestPage = friendRequestRepository.findAllByRecipientGuidAndStatusAndCreatedAtAfter(
                requester.getGuid(),
                FriendRequestStatus.PENDING,
                liveSince,
                pageable
        );

        Map<UUID, User> requesters = userRepository.findAllByGuidIn(
                        friendRequestPage.get()
                                .map(FriendRequest::getRequesterGuid)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        PagedModel<FriendRequestResponse> friendRequestResponses = friendRequestMapper.toPagedModel(
                friendRequestPage,
                friendRequest -> friendRequestMapper.toResponse(
                        friendRequest,
                        userMapper.toResponse(
                                requesters.get(friendRequest.getRequesterGuid())
                        ),
                        userMapper.toResponse(requester)
                )
        );

        return FriendRequestResponseList.builder()
                .friendRequests(friendRequestResponses)
                .incomingCount(friendRequestPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public FriendRequestResponseList getOutgoing(Pageable pageable, AuthenticationToken authenticationToken) {
        User requester = userRepository.findByGuid(authenticationToken.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, authenticationToken.getGuid())));

        if (!permissionValidator.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.READ,
                permissionHelper.getContext(requester.getGuid(), authenticationToken),
                authenticationToken
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        Instant liveSince = Instant.now().minus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS);

        Page<FriendRequest> friendRequestPage = friendRequestRepository.findAllByRequesterGuidAndStatusAndCreatedAtAfter(
                requester.getGuid(),
                FriendRequestStatus.PENDING,
                liveSince,
                pageable
        );

        Map<UUID, User> recipients = userRepository.findAllByGuidIn(
                        friendRequestPage.get()
                                .map(FriendRequest::getRecipientGuid)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        PagedModel<FriendRequestResponse> friendRequestResponses = friendRequestMapper.toPagedModel(
                friendRequestPage,
                friendRequest -> friendRequestMapper.toResponse(
                        friendRequest,
                        userMapper.toResponse(requester),
                        userMapper.toResponse(
                                recipients.get(friendRequest.getRecipientGuid())
                        )
                )
        );

        return FriendRequestResponseList.builder()
                .friendRequests(friendRequestResponses)
                .incomingCount(friendRequestPage.getTotalElements())
                .build();
    }
}
