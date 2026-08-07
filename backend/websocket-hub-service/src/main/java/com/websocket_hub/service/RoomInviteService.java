package com.websocket_hub.service;

import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
import com.redis_starter.repository.RedisHashRepository;
import com.redis_starter.repository.RedisSetRepository;
import com.security_starter.config.AuthenticationToken;
import com.websocket_hub.domain.dto.request.RoomInviteRequest;
import com.websocket_hub.domain.dto.response.RoomInviteResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.serializer.RedisDeserializer;
import com.websocket_hub.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_FINISHED;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_IN_PROGRESS;
import static com.websocket_hub.config.ResourceMessageConstants.SERVICE_UNAVAILABLE;
import static com.websocket_hub.config.ResourceMessageConstants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomInviteService {

    private static final String FRIENDS_KEY_PREFIX = "friends:";
    private static final String ROOM_INVITE_KEY_PREFIX = "room:invite:";
    private static final String MEMBER_DELIMITER = ":";

    private static final int ROOM_INVITE_LIMIT = 10;
    private static final int ZERO_INVITED_USERS = 0;
    private static final Duration ROOM_INVITE_TTL = Duration.ofMinutes(2);

    private static final String NOT_FRIEND = "NOT_FRIEND";
    private static final String ALREADY_IN_ROOM = "ALREADY_IN_ROOM";
    private static final String ALREADY_INVITED = "ALREADY_INVITED";
    private static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";

    private final RoomService roomService;

    private final RedisSetRepository redisSetRepository;

    private final RedisHashRepository redisHashRepository;

    private final KafkaMessageHelper kafkaMessageHelper;

    private final RedisDeserializer  redisDeserializer;

    public void create(RoomInviteRequest request, AuthenticationToken token) {
        Room room = roomService.getByIdAndType(request.roomId(), request.roomType());

        validateRoomInvitable(room, room.getType());

        Set<ClientSession> participants = room.getParticipants();

        ClientSession client = participants
                .stream()
                .filter(participant -> participant.getGuid().equals(token.getGuid()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Set<UUID> friendGuids = redisSetRepository.get(friendsKey(client.getGuid()))
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        if (!friendGuids.contains(request.friendGuid())) {
            throw new BadRequestException(NOT_FRIEND);
        }

        Set<UUID> participantsGuids = participants.stream()
                .map(ClientSession::getGuid)
                .collect(Collectors.toSet());

        if (participantsGuids.contains(request.friendGuid())) {
            throw new BadRequestException(ALREADY_IN_ROOM);
        }

        @SuppressWarnings("unchecked")
        Set<UUID>  invitedFriends= redisDeserializer.deserialize(
                redisHashRepository.findByKey(
                        roomInviteKey(room.getId()), client.getGuid().toString()
                ),
                Set.class
        );

        if (invitedFriends.size()>=ROOM_INVITE_LIMIT) {
            throw new BadRequestException(LIMIT_EXCEEDED);
        }

        if (invitedFriends.contains(request.friendGuid())) {
            throw new BadRequestException(ALREADY_INVITED);
        }

        // todo: доделать запись в кеш с ttl + отправка нотификации
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

    private String friendsKey(UUID guid) {
        return FRIENDS_KEY_PREFIX + guid;
    }

    private String roomInviteKey(UUID roomId) {
        return ROOM_INVITE_KEY_PREFIX + roomId;
    }
}
