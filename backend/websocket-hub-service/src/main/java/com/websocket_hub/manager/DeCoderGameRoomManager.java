package com.websocket_hub.manager;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.PlayerBetFactory;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.mapper.DeCoderGameMessageMapper;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.PlayerBetValidator;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;

import java.util.UUID;

@Service
@Slf4j
public class DeCoderGameRoomManager extends AbstractRoomManager {

    private final DeCoderGameMessageMapper deCoderGameMessageMapper;

    private final SessionManager sessionManager;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final GameServiceClient gameServiceClient;

    public DeCoderGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            DeCoderGameMessageMapper deCoderGameMessageMapper,
            PlayerBetFactory playerBetFactory,
            PlayerBetValidator playerBetValidator,
            RoomValidator roomValidator,
            SessionManager sessionManager,
            RoomRedisRepository roomRedisRepository,
            GameServiceClient gameServiceClient
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator, roomRedisRepository);
        this.deCoderGameMessageMapper = deCoderGameMessageMapper;
        this.sessionManager = sessionManager;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.gameServiceClient = gameServiceClient;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public MessageMapper getMapper() {
        return this.deCoderGameMessageMapper;
    }

    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.DE_CODER_ROOM;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player {} joined DeCoder room {}", user.username(), room.getName());

        broadcast(room.getId(), deCoderGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player={" + user.username() + "} has joined the room={" + room.getName() + "}"
        ));

        sendGameState(user, room.getId());
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user,  Room room, WebSocketSession session) {
        log.info("Player {} left DeCoder room {}", user.username(), room.getName());

        broadcast(room.getId(), deCoderGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player={" + user.username() + "} has left the room={" + room.getName() + "}"
        ));
    }

    @Override
    protected void onCreateRoom(Room room) {
        try {
            log.info("Starting De-Coder game for room {}", room.getId());

            DeCoderGameInternalRequest startRequest = deCoderGameMessageMapper.toStartRequest(DeCoderGameEvent.START, room.getId());

            gameServiceClient.startDeCoderGame(startRequest);

            updateRoomStatus(room.getId(), RoomStatus.IN_PROGRESS);

        } catch (Exception e) {
            log.error("Failed to start De-Coder game for room {}", room.getId(), e);
        }
    }

    @Override
    protected void onDeleteRoom(UUID roomId) {

    }

    public PlayerBet  markPlayerBet(UserInternalResponse user, BigDecimal bet) {
        PlayerBet newPlayerBet = playerBetFactory.create(user.guid(), bet, user.balance());

        playerBetValidator.validateBet(newPlayerBet);

        return newPlayerBet;
    }

    public void sendGameState(UserInternalResponse user, UUID roomId) {
        try {
            var stateResponse = gameServiceClient.getDeCoderGameState(roomId)
                    .orElseThrow(() -> new RuntimeException("Empty state from game-service on refresh"));

            ClientSession clientSession = getClientSessionByGuid(user.guid());
            if (clientSession == null || !clientSession.isOpen()) {
                return;
            }

            DeCoderGameMessage stateMessage = deCoderGameMessageMapper.toMessage(
                    stateResponse, MessageType.SYSTEM, null, user.guid()
            );

            sessionManager.sendToSession(clientSession, stateMessage);

        } catch (Exception e) {
            log.error("Failed to re-send game state for user {}", user.username(), e);
        }
    }
}