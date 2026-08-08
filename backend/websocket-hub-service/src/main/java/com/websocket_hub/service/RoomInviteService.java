package com.websocket_hub.service;

import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
import com.redis_starter.repository.RedisHashRepository;
import com.security_starter.config.AuthenticationToken;
import com.websocket_hub.domain.dto.RoomInviteResponseList;
import com.websocket_hub.domain.dto.request.RoomInviteRequest;
import com.websocket_hub.domain.dto.response.RoomInviteResponse;
import com.websocket_hub.domain.dto.response.UserResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.entity.User;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.repository.FriendshipRepository;
import com.websocket_hub.mapper.UserMapper;
import com.websocket_hub.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_FINISHED;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_IN_PROGRESS;
import static com.websocket_hub.config.ResourceMessageConstants.SERVICE_UNAVAILABLE;
import static com.websocket_hub.config.ResourceMessageConstants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomInviteService {

    private static final String ROOM_INVITE_KEY_PREFIX = "room:invite:";
    private static final String KEY_DELIMITER = ":";

    private static final int ROOM_INVITE_LIMIT = 10;
    private static final Duration ROOM_INVITE_TTL = Duration.ofMinutes(2);

    private static final String NOT_FRIEND = "NOT_FRIEND";
    private static final String ALREADY_IN_ROOM = "ALREADY_IN_ROOM";
    private static final String ALREADY_INVITED = "ALREADY_INVITED";
    private static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";

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

        ClientSession client = participants
                .stream()
                .filter(participant -> participant.getGuid().equals(token.getGuid()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

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
                .orElseThrow(() -> new ForbiddenException(USER_NOT_FOUND));

        Set<UUID> excludedUserGuids = participants.stream()
                .map(ClientSession::getGuid)
                .collect(Collectors.toSet());

        findAllInvites(roomInviteKey(roomId, client.getGuid()))
                .keySet()
                .stream()
                .map(UUID::fromString)
                .forEach(excludedUserGuids::add);

        Page<User> inviteUsers = friendshipRepository.findInviteUsers(client.getGuid(), excludedUserGuids, pageable);

        return buildRoomInviteResponseList(inviteUsers, userMapper::toResponse);
    }

    private RoomInviteResponseList buildRoomInviteResponseList(Page<User> inviteUsers, Function<User, UserResponse> mapping) {
        return RoomInviteResponseList.builder()
                .users(new PagedModel<>(
                        inviteUsers.map(mapping)
                ))
                .build();
    }
}
