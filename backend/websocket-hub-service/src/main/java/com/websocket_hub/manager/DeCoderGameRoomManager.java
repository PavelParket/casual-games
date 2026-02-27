package com.websocket_hub.manager;

import com.websocket_hub.client.DeCoderGameServiceClient;
import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.DeCoderGameEvent;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.PlayerBetFactory;
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

    private final MessageMapper messageMapper;

    private final SessionManager sessionManager;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final DeCoderGameServiceClient deCoderGameServiceClient;

    public DeCoderGameRoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> roomFactory,
            DeCoderGameMessageMapper deCoderGameMessageMapper,
            PlayerBetFactory playerBetFactory,
            PlayerBetValidator playerBetValidator,
            RoomValidator roomValidator,
            SessionManager sessionManager,
            DeCoderGameServiceClient deCoderGameServiceClient
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator);
        this.messageMapper = deCoderGameMessageMapper;
        this.sessionManager = sessionManager;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.deCoderGameServiceClient = deCoderGameServiceClient;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public MessageMapper getMapper() {
        return this.messageMapper;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player {} joined DeCoder room {}", user.username(), room.getName());

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player={" + user.username() + "} has joined the room={" + room.getName() + "}"
        ));

        sendGameStateAsync(user, room.getId());
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user,  Room room, WebSocketSession session) {
        log.info("Player {} left DeCoder room {}", user.username(), room.getName());

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player={" + user.username() + "} has left the room={" + room.getName() + "}"
        ));
    }

    public PlayerBet  markPlayerBet(UserInternalResponse user, BigDecimal bet) {
        PlayerBet newPlayerBet = playerBetFactory.create(user.guid(), bet, user.balance());

        playerBetValidator.validateBet(newPlayerBet);

        return newPlayerBet;
    }

    public void sendGameStateAsync(UserInternalResponse user, UUID roomId) {
        Thread.ofVirtual().start(() -> {
            try {
                String deCoderGameState = deCoderGameServiceClient.getGameState(roomId);

                if (deCoderGameState == null || deCoderGameState.isEmpty()) {
                    return;
                }

                ClientSession clientSession = getClientSessionByGuid(user.guid());
                if (clientSession == null || !clientSession.isOpen()) {
                    log.debug("User {} disconnected before receiving game state", user.username());
                    return;
                }

                DeCoderGameMessage stateMessage = DeCoderGameMessage.builder()
                        .type(MessageType.SYSTEM)
                        .event(DeCoderGameEvent.STATE)
                        .roomId(roomId)
                        .toUserId(user.guid())
                        .gameState(deCoderGameState)
                        .message("Current game state loaded")
                        .build();

                sessionManager.sendToSession(clientSession, stateMessage);
                log.info("Sent game state to user {}", user.username());

            } catch (Exception e) {
                log.error("Failed to fetch/send game state for user {}", user.username(), e);
            }
        });
    }
}