package com.websocket_hub.handler;

import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.entity.PlayerBet;
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

        log.info("DeCoder action: {}", deCoderGameMessage);

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


    }

    private void handleGameMove(UUID roomId, UserInternalResponse user, DeCoderGameMessage message) {
        PlayerBet movePlayerBet = roomManager.markPlayerBet(user, MOVE_COST);

        //TODO: Проблема обновления баланса после каждого хода требует комплексного решения, затронет общие для всех TransactionInternalRequest файлы.
        DeCoderTransactionRequest deCoderTransactionRequest = gameTransactionMapper.toDeCoderRequest(
                roomId,
                roomManager.getRoomType(),
                movePlayerBet,
                null
        );

        try {
            GameTransactionResponse moveTransactionResponse = grpcGameTransactionClient.saveDeCoderGameTransaction(deCoderTransactionRequest);
            if (moveTransactionResponse != null) {
                log.info("Bank service debited {}", MOVE_COST);
            }
        } catch (Exception e) {
            log.warn("Bank service rejected move for {}. Aborting.", user.username());
            throw new GameException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        try {
            DeCoderGameInternalRequest moveRequest = deCoderGameMessageMapper.toMoveRequest(
                    DeCoderGameEvent.MOVE,
                    roomId,
                    user.guid(),
                    message.code()
            );

            DeCoderGameInternalResponse moveResponse = gameServiceClient.processDeCoderMove(moveRequest)
                    .orElseThrow(() -> new RuntimeException("Empty response from game-service"));

            DeCoderGameMessage broadcastMessage = deCoderGameMessageMapper.toMessage(
                    moveResponse, MessageType.SYSTEM, null, null
            );

            if (DeCoderGameEvent.WINNER.equals(moveResponse.event())) {
                handleWin(roomId, user, broadcastMessage);
            } else {
                roomManager.broadcast(roomId, broadcastMessage);
            }
        } catch (Exception e) {
            log.warn("Game move failed. Refunding {} CGC to user {}", MOVE_COST, user.username());
            try {
                PlayerBet refundPlayerBet = roomManager.markPlayerBet(user, MOVE_COST);
                DeCoderTransactionRequest refundRequest = gameTransactionMapper.toDeCoderRequest(
                        roomId, roomManager.getRoomType(), refundPlayerBet, user.guid()
                );
                grpcGameTransactionClient.saveDeCoderGameTransaction(refundRequest);
                log.info("Refund successful for user {}", user.username());
            } catch (Exception refundEx) {
                log.error("CRITICAL: Failed to refund user {} after game error!", user.username(), refundEx);
            }

            throw e;
        }
    }

    private void handleWin(UUID roomId, UserInternalResponse user, DeCoderGameMessage gameResponse) {
        log.info("Player {} won in room {}!", user.username(), roomId);

        try {
            PlayerBet rewardBet = roomManager.markPlayerBet(user, gameResponse.jackpot());

            DeCoderTransactionRequest creditRequest = gameTransactionMapper.toDeCoderRequest(
                    roomId,
                    roomManager.getRoomType(),
                    rewardBet,
                    user.guid()
            );

            GameTransactionResponse transactionResponse = grpcGameTransactionClient.saveDeCoderGameTransaction(creditRequest);

            if (transactionResponse != null) {
                log.info("Bank service credited jackpot for user: {}", user.guid());
            }
        } catch (Exception e) {
            log.error("CRITICAL: Failed to process reward transaction for user {}", user.email(), e);
        } finally {
            roomManager.broadcast(roomId, gameResponse);
            roomManager.updateRoomStatus(roomId, RoomStatus.FINISHED);
        }
    }

    private void handleGetGameState(UUID roomId, UserInternalResponse user) {
        roomManager.sendGameState(user, roomId);
    }
}
