package com.websocket_hub.handler;

import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.entity.DecoderPlayerSpending;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.manager.DeCoderGameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.DeCoderGameMessageMapper;
import com.websocket_hub.mapper.DefaultMessageMapper;
import com.websocket_hub.mapper.GameTransactionMapper;
import com.websocket_hub.serializer.MessageDeserializer;
import com.websocket_hub.service.grpc.client.GrpcGameTransactionClient;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DeCoderGameRoomHandler extends AppWebSocketHandler<DeCoderGameRoomManager> {

    private static final BigDecimal MOVE_COST = new BigDecimal("10.00");

    private final DeCoderGameMessageMapper deCoderGameMessageMapper;

    private final GameTransactionMapper gameTransactionMapper;

    private final GameServiceClient gameServiceClient;

    private final GrpcGameTransactionClient grpcGameTransactionClient;

    public DeCoderGameRoomHandler(
            SessionManager sessionManager,
            DeCoderGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DefaultMessageMapper defaultMessageMapper,
            DeCoderGameMessageMapper deCoderGameMessageMapper,
            GameTransactionMapper gameTransactionMapper,
            GameServiceClient gameServiceClient,
            GrpcGameTransactionClient grpcGameTransactionClient

    ) {
        super(sessionManager, roomManager, errorHandler, messageDeserializer, defaultMessageMapper);
        this.deCoderGameMessageMapper = deCoderGameMessageMapper;
        this.gameTransactionMapper = gameTransactionMapper;
        this.gameServiceClient = gameServiceClient;
        this.grpcGameTransactionClient = grpcGameTransactionClient;
    }

    @Override
    protected void handleMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().isEmpty()) {
            return;
        }

        DeCoderGameMessage deCoderGameMessage = messageDeserializer.deserialize(message.getPayload(), DeCoderGameMessage.class);
        UUID roomId = WebSocketUtil.getRoomId(session);
        UserInternalResponse user = WebSocketUtil.getUser(session);

        switch (deCoderGameMessage.event()) {
            case MOVE -> handleGameMove(roomId, user, deCoderGameMessage);

            case STATE -> handleGetGameState(roomId, user);

            default -> log.warn("Unknown event: {}", deCoderGameMessage.event());
        }
    }

    @Override
    protected void onJoin(UUID roomId, UserInternalResponse user) {

    }

    @Override
    protected void onLeave(UUID roomId, UserInternalResponse user) {
        roomManager.getActiveSpending(roomId).stream()
                .filter(s -> s.getUserGuid().equals(user.guid()))
                .filter(s -> s.getSpent().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .ifPresent(spending -> {

                    try {
                        DeCoderTransactionRequest request = gameTransactionMapper.toDeCoderRequest(
                                roomId,
                                roomManager.getRoomType(),
                                List.of(spending),
                                null,
                                null
                        );

                        grpcGameTransactionClient.saveDeCoderGameResults(request);

                        roomManager.markProcessed(roomId, user.guid());
                    } catch (Exception e) {
                        log.error("Failed to settle DeCoder spending on leave: player={}, room={}", user.guid(), roomId, e);
                    }
                });
    }

    private void handleGameMove(UUID roomId, UserInternalResponse user, DeCoderGameMessage message) {
        if (!roomManager.isValidSpending(roomId, user.guid(), MOVE_COST, user.balance())) {
            throw new GameException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        DeCoderGameInternalRequest moveRequest = deCoderGameMessageMapper.toMoveRequest(
                DeCoderGameEvent.MOVE, roomId, user.guid(), message.code()
        );

        DeCoderGameInternalResponse moveResponse = gameServiceClient.processDeCoderMove(moveRequest)
                .orElseThrow(() -> new RuntimeException("Empty response from game-service"));

        roomManager.incrementSpent(roomId, user.guid(), MOVE_COST);

        DecoderPlayerSpending spending = roomManager.getPlayerSpending(roomId, user.guid())
                .orElse(null);

        DeCoderGameMessage moverMessage = deCoderGameMessageMapper.toMessage(
                moveResponse,
                MessageType.SYSTEM,
                null,
                user.guid(),
                spending != null ? spending.getBalanceBefore() : null,
                spending != null ? spending.getSpent() : null
        );

        DeCoderGameMessage othersMessage = deCoderGameMessageMapper.toMessage(
                moveResponse,
                MessageType.SYSTEM,
                null,
                null,
                null,
                null
        );

        if (DeCoderGameEvent.WINNER.equals(moveResponse.event())) {
            handleWin(roomId, user, moveResponse, moverMessage, othersMessage);
        } else {
            roomManager.broadcastMove(roomId, user.guid(), moverMessage, othersMessage);
        }
    }

    private void handleWin(UUID roomId,
                           UserInternalResponse user,
                           DeCoderGameInternalResponse moveResponse,
                           DeCoderGameMessage moverMessage,
                           DeCoderGameMessage othersMessage) {
        try {
            List<DecoderPlayerSpending> allSpending = roomManager.getActiveSpending(roomId);

            if (!allSpending.isEmpty()) {
                DeCoderTransactionRequest request = gameTransactionMapper.toDeCoderRequest(
                        roomId,
                        roomManager.getRoomType(),
                        allSpending,
                        user.guid(),
                        moveResponse.jackpot()
                );

                grpcGameTransactionClient.saveDeCoderGameResults(request);
                allSpending.forEach(s -> roomManager.markProcessed(roomId, s.getUserGuid()));
            }
        } catch (Exception e) {
            log.error("Failed to process batch transaction: room={}, winner={}", roomId, user.guid(), e);
        } finally {
            roomManager.broadcastMove(roomId, user.guid(), moverMessage, othersMessage);
            roomManager.updateRoomStatus(roomId, RoomStatus.FINISHED);
        }
    }

    private void handleGetGameState(UUID roomId, UserInternalResponse user) {
        roomManager.sendGameState(user, roomId);
    }
}
