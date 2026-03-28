package com.websocket_hub.handler;

import com.websocket_hub.client.BankServiceClient;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.manager.DeCoderGameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.DeCoderGameMessageMapper;
import com.websocket_hub.mapper.DeCoderGameTransactionMapper;
import com.websocket_hub.serializer.MessageDeserializer;
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

    private final MessageDeserializer messageDeserializer;

    private final DeCoderGameMessageMapper deCoderGameMessageMapper;

    private final DeCoderGameTransactionMapper deCoderGameTransactionMapper;

    private final GameServiceClient gameServiceClient;

    private final BankServiceClient bankServiceClient;


    public DeCoderGameRoomHandler(
            SessionManager sessionManager,
            DeCoderGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DeCoderGameMessageMapper deCoderGameMessageMapper,
            DeCoderGameTransactionMapper deCoderGameTransactionMapper,
            GameServiceClient gameServiceClient,
            BankServiceClient bankServiceClient

    ) {
        super(sessionManager, roomManager, errorHandler);
        this.messageDeserializer = messageDeserializer;
        this.deCoderGameMessageMapper = deCoderGameMessageMapper;
        this.deCoderGameTransactionMapper = deCoderGameTransactionMapper;
        this.gameServiceClient = gameServiceClient;
        this.bankServiceClient = bankServiceClient;
    }

    @Override
    protected void handleMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().isEmpty()) {
            return;
        }

        String payload = message.getPayload();

        try {
            DeCoderGameMessage deCoderGameMessage = messageDeserializer.deserialize(payload, DeCoderGameMessage.class);
            UUID roomId = WebSocketUtil.getRoomId(session);
            UserInternalResponse user = WebSocketUtil.getUser(session);

            log.info("DeCoder action: {}", deCoderGameMessage);

            switch (deCoderGameMessage.event()) {
                case MOVE -> handleGameMove(roomId, user, deCoderGameMessage);

                case STATE -> handleGetGameState(roomId, user);

                case null, default -> log.warn("Unknown event: {}", deCoderGameMessage.event());
            }

        } catch (Exception e) {
            log.error("Failed to handle DeCoder message", e);
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
        DeCoderTransactionInternalRequest deCoderTransactionRequest = deCoderGameTransactionMapper.toInternalRequest(
                roomId,
                roomManager.getRoomType(),
                movePlayerBet,
                null
        );

        try {
            DeCoderTransactionInternalResponse moveTransactionResponse = bankServiceClient.sendDeCoderGameTransaction(deCoderTransactionRequest);

            if (moveTransactionResponse != null) {
                log.info("Bank service debited {}: {}", MOVE_COST, moveTransactionResponse.message());
            }
        } catch (Exception e) {
            log.warn("Bank service rejected move for {}. Aborting.", user.username());
            handleGameException(user.guid(), roomId, new RuntimeException("Insufficient funds or bank unavailable."));
            return;
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
                DeCoderTransactionInternalRequest refundRequest = deCoderGameTransactionMapper.toInternalRequest(
                        roomId,
                        roomManager.getRoomType(),
                        refundPlayerBet,
                        user.guid()
                );
                bankServiceClient.sendDeCoderGameTransaction(refundRequest);
                log.info("Refund successful for user {}", user.username());
            } catch (Exception refundEx) {
                log.error("CRITICAL: Failed to refund user {} after game error!", user.username(), refundEx);
            }

            handleGameException(user.guid(), roomId, e);
        }
    }

    private void handleWin(UUID roomId, UserInternalResponse user, DeCoderGameMessage gameResponse) {
        log.info("Player {} won in room {}!", user.username(), roomId);

        try {
            PlayerBet rewardBet = roomManager.markPlayerBet(user, gameResponse.jackpot());

            DeCoderTransactionInternalRequest creditRequest = deCoderGameTransactionMapper.toInternalRequest(
                    roomId,
                    roomManager.getRoomType(),
                    rewardBet,
                    user.guid()
            );

            DeCoderTransactionInternalResponse transactionResponse = bankServiceClient.sendDeCoderGameTransaction(creditRequest);

            if (transactionResponse != null) {
                log.info("Bank service credited jackpot: {}", transactionResponse.message());
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

    private void handleGameException(UUID userId, UUID roomId, Exception e) {
        String errorMessage = "Unexpected game error";

        if (e instanceof RuntimeException && e.getMessage() != null) {
            if (e.getMessage().startsWith("COOLDOWN:")) {
                String seconds = e.getMessage().split(":")[1];
                errorMessage = "Too fast! Please wait " + seconds + " seconds.";
            } else if (e.getMessage().startsWith("Game Error:")) {
                errorMessage = e.getMessage().replace("Game Error: ", "");
            }
        } else {
            log.error("Error in game flow for room {}", roomId, e);
        }

        ClientSession session = sessionManager.getByGuid(userId);
        if (session != null) {
            DeCoderGameMessage errorMsg = DeCoderGameMessage.builder()
                    .type(MessageType.SYSTEM)
                    .event(DeCoderGameEvent.ERROR)
                    .roomId(roomId)
                    .toUserId(userId)
                    .message(errorMessage)
                    .build();

            sessionManager.sendToSession(session, errorMsg);
        }
    }
}
