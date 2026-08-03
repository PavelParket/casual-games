package casualgames.userservice.service;

import casualgames.userservice.domain.dto.FriendshipResponse;
import casualgames.userservice.domain.dto.FriendshipResponseList;
import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import casualgames.userservice.mapper.FriendshipMapper;
import casualgames.userservice.repository.FriendRequestRepository;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.helper.PermissionHelper;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_DELETE_FRIEND;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_FRIENDSHIP;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final FriendshipMapper friendshipMapper;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    private final UserService userService;

    private final UserRepository userRepository;

    @Transactional
    public FriendshipResponse create(User user, User friend, Collection<FriendRequest> requests, AuthenticationToken authenticationToken) {
        Friendship friendship = friendshipRepository.findByUserGuidAndFriendGuid(user.getGuid(), friend.getGuid())
                .orElseGet(() -> friendshipRepository.save(
                        Friendship.builder()
                                .userGuid(user.getGuid())
                                .friendGuid(friend.getGuid())
                                .build()
                ));

        requests.forEach(friendRequest -> {
            friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequest.setResolvedAt(Instant.now());
        });

        friendRequestRepository.saveAll(requests);

        return buildResponse(friendship, user, friend, authenticationToken);
    }

    private FriendshipResponse buildResponse(Friendship friendship,
                                             User user,
                                             User friend,
                                             AuthenticationToken token) {
        return friendshipMapper.toResponse(
                friendship,
                userService.buildResponse(user, permissionHelper.getContext(user.getGuid(), token), token),
                userService.buildResponse(friend, permissionHelper.getContext(friend.getGuid(), token), token)
        );
    }

    @Transactional(readOnly = true)
    public FriendshipResponseList getByUserGuid(UUID userGuid, Pageable pageable, AuthenticationToken authenticationToken) {
        User user = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)));

        if (!permissionHelper.hasAccess(Permissions.FRIEND, Operation.READ, userGuid, authenticationToken)) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND);
        }

        Page<Friendship> friendshipPage = friendshipRepository.findByUserGuid(user.getGuid(), pageable);

        Map<UUID, User> friendsMap = userRepository.findAllByGuidIn(
                        friendshipPage.getContent()
                                .stream()
                                .map(friendship -> getFriendGuid(friendship, userGuid))
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        return FriendshipResponseList.builder()
                .friends(friendshipMapper.toPagedModel(
                        friendshipPage,
                        friendship -> buildResponse(
                                friendship,
                                user,
                                friendsMap.get(getFriendGuid(friendship, userGuid)),
                                authenticationToken
                        )
                ))
                .build();
    }

    private UUID getFriendGuid(Friendship friendship, UUID userGuid) {
        return friendship.getUserGuid().equals(userGuid)
                ? friendship.getFriendGuid()
                : friendship.getUserGuid();
    }

    @Transactional
    public void delete(UUID friendGuid, AuthenticationToken authenticationToken) {
        if (!permissionHelper.hasAccess(Permissions.FRIEND, Operation.DELETE, authenticationToken.getGuid(), authenticationToken)) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_DELETE_FRIEND);
        }

        Friendship friendship = friendshipRepository.findByUserGuidAndFriendGuid(authenticationToken.getGuid(), friendGuid)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIENDSHIP));

        friendshipRepository.delete(friendship);
    }
}
