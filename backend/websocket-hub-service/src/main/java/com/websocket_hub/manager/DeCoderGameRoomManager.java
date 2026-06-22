package com.websocket_hub.manager;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.DecoderPlayerSpending;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.domain.enums.model.SpendingType;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.mapper.DeCoderGameMessageMapper;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DeCoderGameRoomManager extends AbstractRoomManager {

    private final Map<UUID, Map<UUID, DecoderPlayerSpending>> roomPlayerSpendingMap = new ConcurrentHashMap<>();

    private final DeCoderGameMessageMapper deCoderGameMessageMapper;

    private final SessionManager sessionManager;

    private final GameServiceClient gameServiceClient;

    public DeCoderGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            DeCoderGameMessageMapper deCoderGameMessageMapper,
            RoomValidator roomValidator,
            SessionManager sessionManager,
            RoomRedisRepository roomRedisRepository,
            GameServiceClient gameServiceClient
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator, roomRedisRepository);
        this.deCoderGameMessageMapper = deCoderGameMessageMapper;
        this.sessionManager = sessionManager;
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
        log.debug("Player {} joined DeCoder room {}", user.username(), room.getName());

        Map<UUID, DecoderPlayerSpending> playerMap = roomPlayerSpendingMap.get(room.getId());

        if (playerMap != null) {
            DecoderPlayerSpending existing = playerMap.get(user.guid());

            if (existing != null && existing.getType() == SpendingType.PROCESSED) {
                playerMap.remove(user.guid());
            }
        }

        broadcast(room.getId(), deCoderGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));

        sendGameState(user, room.getId());
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.debug("Player {} left DeCoder room {}", user.username(), room.getName());

        broadcast(room.getId(), deCoderGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has left the room " + room.getName()
        ));
    }

    @Override
    protected void onCreateRoom(Room room) {
        try {
            log.debug("Starting De-Coder game for room {}", room.getId());

            DeCoderGameInternalRequest startRequest = deCoderGameMessageMapper.toStartRequest(DeCoderGameEvent.START, room.getId());

            gameServiceClient.startDeCoderGame(startRequest);

            updateRoomStatus(room.getId(), RoomStatus.IN_PROGRESS);

        } catch (Exception e) {
            log.error("Failed to start De-Coder game for room {}", room.getId(), e);
        }
    }

    @Override
    protected void onDeleteRoom(UUID roomId) {
        roomPlayerSpendingMap.remove(roomId);
    }

    public boolean isValidSpending(UUID roomId, UUID guid, BigDecimal cost, BigDecimal currentBalance) {
        DecoderPlayerSpending spending = roomPlayerSpendingMap
                .computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(guid, k ->
                        DecoderPlayerSpending.builder()
                                .userGuid(guid)
                                .balanceBefore(currentBalance)
                                .spent(BigDecimal.ZERO)
                                .type(SpendingType.ACTIVE)
                                .build()
                );

        if (spending.getType() != SpendingType.ACTIVE) {
            return false;
        }

        boolean canAfford = spending.getSpent().add(cost).compareTo(spending.getBalanceBefore()) <= 0;

        if (!canAfford) {
            log.warn("DENIED — balance exhausted: player={}, room={}, balanceBefore={}, spent={}, cost={}", guid, roomId, spending.getBalanceBefore(), spending.getSpent(), cost);
        }

        return canAfford;
    }

    public void incrementSpent(UUID roomId, UUID guid, BigDecimal cost) {
        Map<UUID, DecoderPlayerSpending> playerSpendingMap = roomPlayerSpendingMap.get(roomId);
        if (playerSpendingMap == null) {
            return;
        }

        DecoderPlayerSpending spending = playerSpendingMap.get(guid);
        if (spending == null) {
            return;
        }

        spending.setSpent(spending.getSpent().add(cost));
    }

    public void markProcessed(UUID roomId, UUID guid) {
        Map<UUID, DecoderPlayerSpending> playerSpendingMap = roomPlayerSpendingMap.get(roomId);
        if (playerSpendingMap == null) {
            return;
        }

        DecoderPlayerSpending spending = playerSpendingMap.get(guid);
        if (spending == null) {
            return;
        }

        spending.setType(SpendingType.PROCESSED);
    }

    public Optional<DecoderPlayerSpending> getPlayerSpending(UUID roomId, UUID guid) {
        Map<UUID, DecoderPlayerSpending> playerSpendingMap = roomPlayerSpendingMap.get(roomId);

        if (playerSpendingMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(playerSpendingMap.get(guid));
    }

    public List<DecoderPlayerSpending> getActiveSpending(UUID roomId) {
        Map<UUID, DecoderPlayerSpending> playerSpendingMap = roomPlayerSpendingMap.get(roomId);

        if (playerSpendingMap == null) {
            return List.of();
        }

        return playerSpendingMap.values().stream()
                .filter(spending -> spending.getType() == SpendingType.ACTIVE)
                .toList();
    }

    public void sendGameState(UserInternalResponse user, UUID roomId) {
        try {
            DeCoderGameInternalResponse stateResponse = gameServiceClient.getDeCoderGameState(roomId)
                    .orElseThrow(() -> new RuntimeException("Empty state from game-service on refresh"));

            ClientSession clientSession = getClientSessionByGuid(user.guid());

            if (clientSession == null || !clientSession.isOpen()) {
                return;
            }

            DeCoderGameMessage stateMessage = deCoderGameMessageMapper.toMessage(
                    stateResponse,
                    MessageType.SYSTEM,
                    null,
                    user.guid(),
                    user.balance(),
                    null
            );

            sessionManager.sendToSession(clientSession, stateMessage);

        } catch (Exception e) {
            log.error("Failed to re-send game state for user {}", user.username(), e);
        }
    }

    public void broadcastMove(UUID roomId,
                              UUID toUserGuid,
                              DeCoderGameMessage moverMessage,
                              DeCoderGameMessage othersMessage) {
        Set<ClientSession> players = getPlayersInRoom(roomId);

        if (players.isEmpty()) {
            return;
        }

        List<Thread> threads = new ArrayList<>();

        for (ClientSession client : players) {
            DeCoderGameMessage message = client.getGuid().equals(toUserGuid) ? moverMessage : othersMessage;
            Thread thread = Thread.ofVirtual().start(() -> sendToClient(client, message));
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("broadcastMove interrupted for room {}", roomId);
            }
        }
    }
}