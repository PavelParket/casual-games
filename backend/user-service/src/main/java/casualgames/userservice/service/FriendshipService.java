package casualgames.userservice.service;

import casualgames.userservice.domain.dto.FriendshipResponse;
import casualgames.userservice.domain.dto.FriendshipResponseList;
import casualgames.userservice.domain.dto.UserFriendRequestFilter;
import casualgames.userservice.domain.dto.UserFriendResponse;
import casualgames.userservice.domain.dto.UserFriendResponseList;
import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import casualgames.userservice.domain.enums.FriendshipStatus;
import casualgames.userservice.mapper.FriendshipMapper;
import casualgames.userservice.mapper.UserMapper;
import casualgames.userservice.repository.FriendRequestRepository;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.service.helper.PermissionHelper;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_DELETE_FRIEND;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_FRIENDSHIP;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final FriendshipMapper friendshipMapper;

    private final PermissionHelper permissionHelper;

    private final UserService userService;

    private final UserMapper userMapper;

    @Transactional
    public FriendshipResponse create(User user, User friend, Collection<FriendRequest> requests, AuthenticationToken token) {
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

        return buildResponse(friendship, user, friend, token);
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
    public FriendshipResponseList getByUserGuid(UUID userGuid, Pageable pageable, AuthenticationToken token) {
        User user = userService.getByGuid(userGuid);

        if (!permissionHelper.hasAccess(Permissions.FRIEND, Operation.READ, userGuid, token)) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_READ_FRIEND);
        }

        Page<Friendship> friendshipPage = friendshipRepository.findByUserGuid(user.getGuid(), pageable);

        Map<UUID, User> friendsMap = userService.getByGuidIn(
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
                                token
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
    public void delete(UUID friendGuid, AuthenticationToken token) {
        if (!permissionHelper.hasAccess(Permissions.FRIEND, Operation.DELETE, token.getGuid(), token)) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_DELETE_FRIEND);
        }

        Friendship friendship = friendshipRepository.findByUserGuidAndFriendGuid(token.getGuid(), friendGuid)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_FRIENDSHIP));

        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public UserFriendResponseList search(UserFriendRequestFilter filter, Pageable pageable, AuthenticationToken token) {
        User user = userService.getByGuid(token.getGuid());

        Page<User> userPage = userService.searchByUsername(filter.username(), pageable, token);

        List<UUID> usersGuids = userPage.get()
                .map(User::getGuid)
                .toList();

        Set<UUID> friendGuids = friendshipRepository.findByUserGuidAndUserGuidIn(user.getGuid(), usersGuids)
                .stream()
                .map(friendship -> getFriendGuid(friendship, user.getGuid()))
                .collect(Collectors.toSet());

        List<UUID> nonFriendGuids = usersGuids.stream()
                .filter(guid -> !friendGuids.contains(guid))
                .toList();

        List<FriendRequest> pendingFriendRequests = nonFriendGuids.isEmpty()
                ? List.of()
                : friendRequestRepository.findLatestByRequesterGuidAndRecipientGuidInAndStatusIn(
                user.getGuid(),
                nonFriendGuids,
                List.of(FriendRequestStatus.PENDING.name()));


        Map<UUID, FriendshipStatus> friendshipStatusMap = new HashMap<>();

        pendingFriendRequests.forEach(request -> {
            boolean sentByUser = request.getRequesterGuid().equals(user.getGuid());
            UUID userGuid = sentByUser
                    ? request.getRecipientGuid()
                    : request.getRequesterGuid();

            friendshipStatusMap.put(
                    userGuid,
                    sentByUser
                            ? FriendshipStatus.REQUEST_SENT
                            : FriendshipStatus.REQUEST_RECEIVED
            );
        });

        Page<UserFriendResponse> userFriendResponsePage = userPage.map(userFriend -> userMapper.toResponse(
                userFriend,
                getFriendshipStatus(userFriend.getGuid(), friendGuids, friendshipStatusMap)
        ));

        return UserFriendResponseList.builder()
                .users(new PagedModel<>(userFriendResponsePage))
                .build();
    }

    private FriendshipStatus getFriendshipStatus(UUID userFriendGuid, Set<UUID> friendGuids, Map<UUID, FriendshipStatus> friendshipStatusMap) {
        if (friendGuids.contains(userFriendGuid)) {
            return FriendshipStatus.FRIENDS;
        }

        return friendshipStatusMap.getOrDefault(userFriendGuid, FriendshipStatus.NONE);
    }
}
