package casualgames.userservice.service;

import casualgames.userservice.domain.dto.FriendshipResponse;
import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import casualgames.userservice.mapper.FriendshipMapper;
import casualgames.userservice.repository.FriendRequestRepository;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.service.helper.PermissionHelper;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final FriendshipMapper friendshipMapper;

    private final PermissionHelper permissionHelper;

    private final UserService userService;

    @Transactional
    public FriendshipResponse create(User user, User friend, Collection<FriendRequest> requests, AuthenticationToken authenticationToken) {
        Friendship friendship = friendshipRepository.findByUserGuidAndFriendGuid(user.getGuid(), friend.getGuid())
                .orElseGet(() -> friendshipRepository.save(
                        Friendship.builder()
                                .userGuid(user.getGuid())
                                .friendGuid(friend.getGuid())
                                .build()
                ));

        Instant resolvedAt = Instant.now();

        requests.forEach(friendRequest -> {
            friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequest.setResolvedAt(resolvedAt);
        });

        friendRequestRepository.saveAll(requests);

        PermissionContext context = permissionHelper.getContext(friend.getGuid(), authenticationToken);

        return friendshipMapper.toResponse(
                friendship,
                userService.buildResponse(user, context, authenticationToken),
                userService.buildResponse(friend, context, authenticationToken)
        );
    }
}
