package com.websocket_hub.handler;

import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.MahjongTransactionRequest;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.MahjongGameInternalRequest;
import com.websocket_hub.domain.dto.client.MahjongGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.dto.message.MahjongGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.MahjongBoard;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import com.websocket_hub.domain.enums.events.MahjongGameEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.helper.WebSocketHelper;
import com.websocket_hub.manager.MahjongGameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.DefaultMessageMapper;
import com.websocket_hub.mapper.GameTransactionMapper;
import com.websocket_hub.mapper.MahjongGameMessageMapper;
import com.websocket_hub.serializer.MessageDeserializer;
import com.websocket_hub.service.grpc.client.GrpcGameTransactionClient;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MahjongGameRoomHandler extends AppWebSocketHandler<MahjongGameRoomManager> {

    private final MahjongGameMessageMapper mahjongGameMessageMapper;

    private final GameServiceClient gameServiceClient;

    private final WebSocketHelper webSocketHelper;

    private final GameTransactionMapper gameTransactionMapper;

    private final GrpcGameTransactionClient grpcGameTransactionClient;

    public MahjongGameRoomHandler(
            SessionManager sessionManager,
            MahjongGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DefaultMessageMapper defaultMessageMapper,
            MahjongGameMessageMapper mahjongGameMessageMapper,
            GameServiceClient gameServiceClient,
            WebSocketHelper webSocketHelper,
            GameTransactionMapper gameTransactionMapper,
            GrpcGameTransactionClient grpcGameTransactionClient
    ) {
        super(sessionManager, roomManager, errorHandler, messageDeserializer, defaultMessageMapper);
        this.mahjongGameMessageMapper = mahjongGameMessageMapper;
        this.gameServiceClient = gameServiceClient;
        this.webSocketHelper = webSocketHelper;
        this.gameTransactionMapper = gameTransactionMapper;
        this.grpcGameTransactionClient = grpcGameTransactionClient;
    }

    @Override
    protected void handleMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.isBlank()) {
            log.warn("Received empty message from session {}", session.getId());

            return;
        }

        MahjongGameMessage mahjongGameMessage = messageDeserializer.deserialize(payload, MahjongGameMessage.class);
        UUID roomId = WebSocketUtil.getRoomId(session);
        UserInternalResponse user = WebSocketUtil.getUser(session);

        if (mahjongGameMessage.event() == null) {
            return;
        }

        switch (mahjongGameMessage.event()) {
            case READY -> handlePlayerReady(roomId, user);

            case MOVE -> handleGameMove(mahjongGameMessage, roomId, user);

            case BET -> handlePlayerBet(mahjongGameMessage, roomId, user);

            default -> log.warn("Unhandled mahjong event: {}", mahjongGameMessage.event());
        }
    }

    @Override
    protected void onJoin(UUID roomId, UserInternalResponse user) {

    }

    @Override
    protected void onLeave(UUID roomId, UserInternalResponse user) {

    }

    private void handlePlayerReady(UUID roomId, UserInternalResponse user) {
        roomManager.markReady(roomId, user);

        if (roomManager.areBothPlayersReady(roomId)) {
            startGame(roomId);
            roomManager.removeReadyPlayers(roomId);
        }
    }

    private void startGame(UUID roomId) {
        try {
            roomManager.validateBetsForGameStart(roomId);

            Map<UUID, String> players = roomManager.getPlayersInRoom(roomId).stream()
                    .collect(Collectors.toMap(
                            ClientSession::getGuid,
                            ClientSession::getUsername
                    ));

            if (players.isEmpty()) {
                throw new IllegalStateException("Room is empty!");
            }

            MahjongGameInternalRequest startRequest = mahjongGameMessageMapper.toStartRequest(roomId, players.keySet().stream().toList());

            MahjongGameInternalResponse startResponse = gameServiceClient.startMahjongGame(startRequest);

            for (MahjongBoard board : startResponse.boards()) {
                MahjongGameMessage startMessage = mahjongGameMessageMapper.toStartMessage(
                        board,
                        startResponse.seed(),
                        MessageType.SYSTEM,
                        MahjongGameEvent.START,
                        board.getPlayerGuid(),
                        roomId,
                        players
                );

                webSocketHelper.sendToSession(board.getPlayerGuid(), startMessage);
            }

            roomManager.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);
        } catch (IllegalStateException e) {
            log.warn("Cannot start game in room {}: {}", roomId, e.getMessage());

            roomManager.removeReadyPlayers(roomId);

            roomManager.broadcast(roomId, mahjongGameMessageMapper.toResponse(
                    MessageType.SYSTEM,
                    MahjongGameEvent.START_FAILED,
                    null,
                    null,
                    roomId,
                    e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to start game in room {}: {}", roomId, e.getMessage(), e);

            roomManager.removeReadyPlayers(roomId);

            roomManager.broadcast(roomId, mahjongGameMessageMapper.toResponse(
                    MessageType.SYSTEM,
                    MahjongGameEvent.START_FAILED,
                    null,
                    null,
                    roomId,
                    "Game service unavailable, please try again"
            ));
        }
    }

    private void handleGameMove(MahjongGameMessage mahjongGameMessage, UUID roomId, UserInternalResponse user) {
        MahjongGameInternalRequest moveRequest = mahjongGameMessageMapper.toMoveRequest(
                roomId, user.guid(), mahjongGameMessage.slot1(), mahjongGameMessage.slot2()
        );

        MahjongGameInternalResponse moveResponse = gameServiceClient.processMahjongMove(moveRequest);

        if (!moveResponse.valid()) {
            throw new GameException(ErrorCode.INVALID_MOVE);
        }

        if (moveResponse.cleared()) {
            processGameOver(roomId, user.guid());
            return;
        }

        MahjongGameMessage moverMessage = mahjongGameMessageMapper.toMoverStateMessage(
                moveResponse, MessageType.SYSTEM, MahjongGameEvent.GAME_STATE, user.guid(), roomId
        );

        MahjongGameMessage opponentMessage = mahjongGameMessageMapper.toOpponentStateMessage(
                moveResponse, MessageType.SYSTEM, MahjongGameEvent.GAME_STATE, moveResponse.opponentGuid(), roomId
        );

        roomManager.broadcastMove(roomId, user.guid(), moverMessage, opponentMessage);
    }

    private void processGameOver(UUID roomId, UUID winnerGuid) {
        MahjongGameMessage gameOverMessage = mahjongGameMessageMapper.toGameOverMessage(
                MessageType.SYSTEM, MahjongGameEvent.GAME_OVER, roomId, winnerGuid
        );

        roomManager.broadcast(roomId, gameOverMessage);

        try {
            List<PlayerBet> bets = roomManager.getPlayerBets(roomId);

            MahjongTransactionRequest transactionRequest = gameTransactionMapper.toMahjongRequest(
                    roomId,
                    roomManager.getRoomType(),
                    bets,
                    winnerGuid
            );

            GameTransactionResponse transactionResponse = grpcGameTransactionClient.saveMahjongGameResults(transactionRequest);

            log.info("Bank service response: transactions={}", transactionResponse.getTransactionCount());
        } catch (Exception e) {
            log.error("Failed to process game results for room {}", roomId, e);

            roomManager.broadcast(roomId, ErrorMessage.builder()
                    .type(MessageType.SYSTEM)
                    .event(ErrorEvent.ERROR)
                    .roomId(roomId)
                    .errorCode(ErrorCode.SERVICE_UNAVAILABLE)
                    .message(ErrorCode.SERVICE_UNAVAILABLE.getMessage())
                    .build());
        } finally {
            roomManager.removePlayerBets(roomId);
            roomManager.updateRoomStatus(roomId, RoomStatus.FINISHED);
        }
    }

    private void handlePlayerBet(MahjongGameMessage mahjongGameMessage, UUID roomId, UserInternalResponse user) {
        try {
            roomManager.markPlayerBet(roomId, user, mahjongGameMessage.bet());
        } catch (IllegalArgumentException e) {
            log.warn("Bet rejected for user={} in room={}: {}", user.username(), roomId, e.getMessage());

            ClientSession client = sessionManager.getByGuid(user.guid());

            if (client != null) {
                sessionManager.sendToSession(client, mahjongGameMessageMapper.toResponse(
                        MessageType.SYSTEM,
                        MahjongGameEvent.BET_REJECT,
                        null,
                        user.guid(),
                        roomId,
                        e.getMessage()
                ));
            }
        }
    }
}
