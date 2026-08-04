package casualgames.userservice.service;

import casualgames.userservice.domain.dto.FriendRequestRequest;
import casualgames.userservice.domain.dto.FriendRequestResponse;
import casualgames.userservice.domain.dto.FriendRequestResponseList;
import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import casualgames.userservice.mapper.FriendRequestMapper;
import casualgames.userservice.repository.FriendRequestRepository;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.service.helper.KafkaMessageHelper;
import casualgames.userservice.service.helper.PermissionHelper;
import casualgames.userservice.validator.FriendRequestValidator;
import com.common_utils.enums.NotificationEventParams;
import com.common_utils.enums.NotificationType;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static casualgames.userservice.config.ResourceMessageConstants.BAD_REQUEST_SELF_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_ALREADY_FRIENDS;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_ALREADY_SENT;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_LIMIT_EXCEEDED;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_FRIEND_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {

    public static final int REQUEST_COOLDOWN_DAYS = 1;

    private static final int PENDING_REQUEST_LIMIT = 15;

    private final FriendRequestRepository friendRequestRepository;

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestMapper friendRequestMapper;

    private final PermissionHelper permissionHelper;

    private final UserService userService;

    private final FriendRequestValidator friendRequestValidator;

    private final FriendshipService friendshipService;

    private final KafkaMessageHelper kafkaMessageHelper;

    @Transactional
    public FriendRequestResponse create(UUID recipientGuid, AuthenticationToken authenticationToken) {

        User requester = userService.getByGuid(authenticationToken.getGuid());

        if (requester.getGuid().equals(recipientGuid)) {
            throw new BadRequestException(BAD_REQUEST_SELF_FRIEND_REQUEST);
        }

        User recipient = userService.getByGuid(recipientGuid);

        if (friendshipRepository.findByUserGuidAndFriendGuid(requester.getGuid(), recipient.getGuid())
                .isPresent()) {
            throw new ConflictException(CONFLICT_ALREADY_FRIENDS);
        }

        FriendRequest existingRequestFromRecipient = friendRequestRepository.findLatestByRequesterGuidAndRecipientGuidAndStatusIn(
                        recipient.getGuid(),
                        requester.getGuid(),
                        List.of(FriendRequestStatus.PENDING.name())
                )
                .orElse(null);

        if (existingRequestFromRecipient != null) {
            friendshipService.create(
                    requester,
                    recipient,
                    List.of(existingRequestFromRecipient),
                    authenticationToken
            );

            return buildResponse(existingRequestFromRecipient, requester, recipient, authenticationToken);
        }

        Instant requestCooldownExpiring = Instant.now().minus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS);

        Long existingRequestCount = friendRequestRepository.countByRequesterGuidAndStatusAndCreatedAtAfter(
                requester.getGuid(),
                FriendRequestStatus.PENDING,
                requestCooldownExpiring
        );

        if (existingRequestCount >= PENDING_REQUEST_LIMIT) {
            throw new ConflictException(CONFLICT_REQUEST_LIMIT_EXCEEDED);
        }

        friendRequestRepository.findLatestByRequesterGuidAndRecipientGuidAndStatusIn(
                        requester.getGuid(),
                        recipient.getGuid(),
                        List.of(FriendRequestStatus.DECLINED.name(), FriendRequestStatus.CANCELED.name())
                )
                .ifPresent(friendRequestValidator::validateCooldown);

        FriendRequest existingPendingRequest = friendRequestRepository.findLatestByRequesterGuidAndRecipientGuidAndStatusIn(
                requester.getGuid(),
                recipient.getGuid(),
                List.of(FriendRequestStatus.PENDING.name())
        ).orElse(null);

        if (existingPendingRequest != null) {
            if (existingPendingRequest.getCreatedAt().isAfter(requestCooldownExpiring)) {
                throw new ConflictException(CONFLICT_REQUEST_ALREADY_SENT);
            }

            existingPendingRequest.setStatus(FriendRequestStatus.CANCELED);
            existingPendingRequest.setResolvedAt(existingPendingRequest.getCreatedAt());

            friendRequestRepository.save(existingPendingRequest);
        }

        FriendRequest newRequest = friendRequestRepository.save(
                FriendRequest.builder()
                        .requesterGuid(requester.getGuid())
                        .recipientGuid(recipient.getGuid())
                        .build()
        );

        kafkaMessageHelper.save(
                kafkaMessageHelper.getTopics().getUserNotification(),
                kafkaMessageHelper.buildFriendRequestUpdatedEvent(
                        recipient.getGuid(),
                        newRequest.getId(),
                        NotificationType.FRIEND_REQUEST_RECEIVED,
                        Map.of(NotificationEventParams.USERNAME.getParam(), requester.getUsername())
                )
        );

        return buildResponse(newRequest, requester, recipient, authenticationToken);
    }

    private FriendRequestResponse buildResponse(FriendRequest friendRequest,
                                                User requester,
                                                User recipient,
                                                AuthenticationToken token) {
        return friendRequestMapper.toResponse(
                friendRequest,
                userService.buildResponse(requester, permissionHelper.getContext(requester.getGuid(), token), token),
                userService.buildResponse(recipient, permissionHelper.getContext(recipient.getGuid(), token), token)
        );
    }

    @Transactional
    public FriendRequestResponse update(FriendRequestRequest friendRequestRequest, AuthenticationToken authenticationToken) {
        switch (friendRequestRequest.status()) {
            case ACCEPTED -> {
                return processAccept(friendRequestRequest, authenticationToken);
            }
            case DECLINED -> {
                return processDecline(friendRequestRequest, authenticationToken);
            }
            case CANCELED -> {
                return processCancel(friendRequestRequest, authenticationToken);
            }
            default -> throw new BadRequestException("Invalid friend request status");
        }
    }

    @Transactional
    public FriendRequestResponse processAccept(FriendRequestRequest friendRequestRequest, AuthenticationToken token) {
        User requester = userService.getByGuid(token.getGuid());

        if (!permissionHelper.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                requester.getGuid(),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        FriendRequest request = friendRequestRepository.findById(friendRequestRequest.id())
                .filter(friendRequest -> friendRequest.getRecipientGuid().equals(requester.getGuid()))
                .filter(friendRequest -> FriendRequestStatus.PENDING.equals(friendRequest.getStatus()))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIEND_REQUEST));

        User recipient = userService.getByGuid(request.getRequesterGuid());

        friendshipService.create(requester, recipient, List.of(request), token);

        return buildResponse(request, requester, recipient, token);
    }

    @Transactional
    public FriendRequestResponse processDecline(FriendRequestRequest friendRequestRequest, AuthenticationToken token) {
        User recipient = userService.getByGuid(token.getGuid());

        if (!permissionHelper.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                recipient.getGuid(),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        FriendRequest existingRequest = friendRequestRepository.findById(friendRequestRequest.id())
                .filter(friendRequest -> friendRequest.getRecipientGuid().equals(recipient.getGuid()))
                .filter(friendRequest -> FriendRequestStatus.PENDING.equals(friendRequest.getStatus()))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIEND_REQUEST));

        User requester = userService.getByGuid(existingRequest.getRequesterGuid());

        existingRequest.setStatus(friendRequestRequest.status());
        existingRequest.setResolvedAt(Instant.now());

        return buildResponse(friendRequestRepository.save(existingRequest), requester, recipient, token);
    }

    @Transactional
    public FriendRequestResponse processCancel(FriendRequestRequest friendRequestRequest, AuthenticationToken token) {
        User requester = userService.getByGuid(token.getGuid());

        if (!permissionHelper.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.UPDATE,
                requester.getGuid(),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_FRIEND_REQUEST);
        }

        FriendRequest existingRequest = friendRequestRepository.findById(friendRequestRequest.id())
                .filter(friendRequest -> friendRequest.getRequesterGuid().equals(requester.getGuid()))
                .filter(friendRequest -> FriendRequestStatus.PENDING.equals(friendRequest.getStatus()))
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIEND_REQUEST));

        User recipient = userService.getByGuid(existingRequest.getRecipientGuid());

        existingRequest.setStatus(friendRequestRequest.status());
        existingRequest.setResolvedAt(Instant.now());

        return buildResponse(friendRequestRepository.save(existingRequest), requester, recipient, token);
    }

    @Transactional(readOnly = true)
    public FriendRequestResponseList search(Boolean incoming, Pageable pageable, AuthenticationToken token) {
        User user = userService.getByGuid(token.getGuid());

        if (!permissionHelper.hasAccess(
                Permissions.FRIEND_REQUEST,
                Operation.READ,
                user.getGuid(),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND_REQUEST);
        }

        if (incoming != null) {
            return incoming
                    ? getIncoming(user, pageable, token)
                    : getOutgoing(user, pageable, token);
        }

        return getIncomingAndOutgoing(user, pageable, token);
    }

    private FriendRequestResponseList getIncoming(User user, Pageable pageable, AuthenticationToken token) {
        Page<FriendRequest> friendRequestPage = friendRequestRepository.findAllByRecipientGuidAndStatus(
                user.getGuid(),
                FriendRequestStatus.PENDING,
                pageable
        );

        Map<UUID, User> requesters = userService.getByGuidIn(
                        friendRequestPage.getContent()
                                .stream()
                                .map(FriendRequest::getRequesterGuid)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        return FriendRequestResponseList.builder()
                .incomingFriendRequests(friendRequestMapper.toPagedModel(
                        friendRequestPage,
                        friendRequest -> buildResponse(
                                friendRequest,
                                requesters.get(friendRequest.getRequesterGuid()),
                                user,
                                token
                        )
                ))
                .incomingCount(friendRequestPage.getTotalElements())
                .build();
    }

    private FriendRequestResponseList getOutgoing(User user, Pageable pageable, AuthenticationToken token) {
        Page<FriendRequest> friendRequestPage = friendRequestRepository.findAllByRequesterGuidAndStatus(
                user.getGuid(),
                FriendRequestStatus.PENDING,
                pageable
        );

        Map<UUID, User> recipients = userService.getByGuidIn(
                        friendRequestPage.getContent()
                                .stream()
                                .map(FriendRequest::getRecipientGuid)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        return FriendRequestResponseList.builder()
                .outgoingFriendRequests(friendRequestMapper.toPagedModel(
                        friendRequestPage,
                        friendRequest -> buildResponse(
                                friendRequest,
                                recipients.get(friendRequest.getRecipientGuid()),
                                user,
                                token
                        )
                ))
                .outgoingCount(friendRequestPage.getTotalElements())
                .build();
    }

    private FriendRequestResponseList getIncomingAndOutgoing(User user, Pageable pageable, AuthenticationToken token) {
        FriendRequestResponseList incoming = getIncoming(user, pageable, token);
        FriendRequestResponseList outgoing = getOutgoing(user, pageable, token);

        return FriendRequestResponseList.builder()
                .incomingFriendRequests(incoming.incomingFriendRequests())
                .outgoingFriendRequests(outgoing.outgoingFriendRequests())
                .incomingCount(incoming.incomingCount())
                .outgoingCount(outgoing.outgoingCount())
                .build();
    }
}
