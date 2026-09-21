package com.casualgames.websockethub.service;

import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.commonutils.exception.ServiceUnavailableException;
import com.casualgames.redisstarter.repository.RedisHashRepository;
import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.websockethub.domain.dto.request.RoomInviteRequest;
import com.casualgames.websockethub.domain.dto.response.RoomInviteFriendResponse;
import com.casualgames.websockethub.domain.dto.response.RoomInviteResponse;
import com.casualgames.websockethub.domain.dto.response.RoomInviteResponseList;
import com.casualgames.websockethub.domain.entity.ClientSession;
import com.casualgames.websockethub.domain.entity.Room;
import com.casualgames.websockethub.domain.entity.User;
import com.casualgames.websockethub.domain.enums.RoomInviteFriendStatus;
import com.casualgames.websockethub.domain.enums.RoomType;
import com.casualgames.websockethub.domain.repository.FriendshipRepository;
import com.casualgames.websockethub.mapper.UserMapper;
import com.casualgames.websockethub.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.casualgames.websockethub.config.ResourceMessageConstants.ALREADY_INVITED;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ALREADY_IN_ROOM;
import static com.casualgames.websockethub.config.ResourceMessageConstants.LIMIT_EXCEEDED;
import static com.casualgames.websockethub.config.ResourceMessageConstants.NOT_FRIEND;
import static com.casualgames.websockethub.config.ResourceMessageConstants.NOT_ROOM_PARTICIPANT;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_ALREADY_FINISHED;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_ALREADY_IN_PROGRESS;
import static com.casualgames.websockethub.config.ResourceMessageConstants.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomInviteService {

    private static final String ROOM_INVITE_KEY_PREFIX = "room:invite:";
    private static final String KEY_DELIMITER = ":";

    private static final int ROOM_INVITE_LIMIT = 10;
    private static final Duration ROOM_INVITE_TTL = Duration.ofMinutes(2);

    private final RoomService roomService;

    private final RedisHashRepository redisHashRepository;

    private final FriendshipRepository friendshipRepository;

    private final KafkaMessageHelper kafkaMessageHelper;

    private final UserService userService;

    private final UserMapper userMapper;

    public RoomInviteResponse create(RoomInviteRequest request, AuthenticationToken token) {
        Room room = roomService.getByIdAndType(request.roomId(), request.roomType());

        validateRoomInvitable(room, room.getType());

        Set<ClientSession> participants = room.getParticipants();

        ClientSession client = participants.stream()
                .filter(participant -> participant.getGuid().equals(token.getGuid()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(NOT_ROOM_PARTICIPANT));

        User friend = userService.getByGuid(request.friendGuid());

        if (!isFriend(client.getGuid(), friend.getGuid())) {
            throw new BadRequestException(NOT_FRIEND);
        }

        Set<UUID> participantsGuids = participants.stream()
                .map(ClientSession::getGuid)
                .collect(Collectors.toSet());

        if (participantsGuids.contains(request.friendGuid())) {
            throw new BadRequestException(ALREADY_IN_ROOM);
        }

        String key = roomInviteKey(room.getId(), client.getGuid());
        Map<String, String> invites = findAllInvites(key);

        if (invites.containsKey(friend.getGuid().toString())) {
            throw new BadRequestException(ALREADY_INVITED);
        }

        if (invites.size() >= ROOM_INVITE_LIMIT) {
            throw new BadRequestException(LIMIT_EXCEEDED);
        }

        Instant now = Instant.now();
        Instant expiredAt = now.plus(ROOM_INVITE_TTL);

        putInvite(key, friend.getGuid(), expiredAt);

        kafkaMessageHelper.sendRoomInvitedEvent(friend.getGuid(), client, room, ROOM_INVITE_TTL, now);

        return RoomInviteResponse.builder()
                .invitedUser(userMapper.toResponse(friend))
                .build();
    }

    private boolean isFriend(UUID clientGuid, UUID friendGuid) {
        return friendshipRepository.existsByUserGuidAndFriendGuid(clientGuid, friendGuid);
    }

    private Map<String, String> findAllInvites(String key) {
        try {
            return redisHashRepository.findAllOrThrow(key);
        } catch (DataAccessException e) {
            throw new ServiceUnavailableException(SERVICE_UNAVAILABLE);
        }
    }

    private void putInvite(String key, UUID friendGuid, Instant expireAt) {
        try {
            redisHashRepository.putOrThrow(key, friendGuid.toString(), expireAt.toString());
            redisHashRepository.expireOrThrow(key, ROOM_INVITE_TTL);
        } catch (DataAccessException e) {
            throw new ServiceUnavailableException(SERVICE_UNAVAILABLE);
        }
    }

    private void validateRoomInvitable(Room room, RoomType roomType) {
        switch (room.getStatus()) {
            case FINISHED -> throw new ForbiddenException(ROOM_ALREADY_FINISHED);
            case IN_PROGRESS -> {
                if (!roomType.isAllowsLateJoin()) {
                    throw new ForbiddenException(ROOM_ALREADY_IN_PROGRESS);
                }
            }
            default -> {
            }
        }
    }

    private String roomInviteKey(UUID roomId, UUID clientId) {
        return ROOM_INVITE_KEY_PREFIX + roomId + KEY_DELIMITER + clientId;
    }

    public RoomInviteResponseList getInviteUsers(UUID roomId, RoomType roomType, Pageable pageable, AuthenticationToken token) {
        Room room = roomService.getByIdAndType(roomId, roomType);

        validateRoomInvitable(room, room.getType());

        Set<ClientSession> participants = room.getParticipants();

        ClientSession client = participants.stream()
                .filter(participant -> participant.getGuid().equals(token.getGuid()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(NOT_ROOM_PARTICIPANT));

        Set<UUID> participantGuids = participants.stream()
                .map(ClientSession::getGuid)
                .collect(Collectors.toSet());

        Set<UUID> invitedFriendGuids = findAllInvites(roomInviteKey(roomId, client.getGuid()))
                .keySet()
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        Page<User> friends = friendshipRepository.findAllFriends(client.getGuid(), pageable);

        return buildRoomInviteResponseList(friends, participantGuids, invitedFriendGuids);
    }

    private RoomInviteResponseList buildRoomInviteResponseList(Page<User> friends, Set<UUID> participantGuids, Set<UUID> invitedGuids) {
        return RoomInviteResponseList.builder()
                .friends(userMapper.toPagedModel(
                        friends.map(friend ->
                                RoomInviteFriendResponse.builder()
                                        .user(userMapper.toResponse(friend))
                                        .status(resolveRoomInviteStatus(friend.getGuid(), participantGuids, invitedGuids))
                                        .build()
                        )
                ))
                .build();
    }

    private RoomInviteFriendStatus resolveRoomInviteStatus(UUID friendGuid, Set<UUID> participantGuids, Set<UUID> invitedGuids) {
        if (participantGuids.contains(friendGuid)) {
            return RoomInviteFriendStatus.ALREADY_IN_ROOM;
        }

        if (invitedGuids.contains(friendGuid)) {
            return RoomInviteFriendStatus.ALREADY_INVITED;
        }

        return RoomInviteFriendStatus.AVAILABLE;
    }
}
