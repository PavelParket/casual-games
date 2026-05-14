package com.websocket_hub.handler;

import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.ErrorMessage;
import com.websocket_hub.domain.dto.message.TicTacToeGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.ErrorEvent;
import com.websocket_hub.domain.enums.events.TicTacToeGameEvent;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.manager.TicTacToeGameRoomManager;
import com.websocket_hub.mapper.DefaultMessageMapper;
import com.websocket_hub.mapper.GameTransactionMapper;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
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
public class TicTacToeGameRoomHandler extends AppWebSocketHandler<TicTacToeGameRoomManager> {

    private final TicTacToeGameMessageMapper ticTacToeGameMessageMapper;

    private final GameTransactionMapper gameTransactionMapper;

    private final GameServiceClient gameServiceClient;

    private final GrpcGameTransactionClient grpcGameTransactionClient;

    public TicTacToeGameRoomHandler(
            SessionManager sessionManager,
            TicTacToeGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DefaultMessageMapper defaultMessageMapper,
            TicTacToeGameMessageMapper ticTacToeGameMessageMapper,
            GameTransactionMapper gameTransactionMapper,
            GameServiceClient gameServiceClient,
            GrpcGameTransactionClient grpcGameTransactionClient
    ) {
        super(sessionManager, roomManager, errorHandler, messageDeserializer, defaultMessageMapper);
        this.ticTacToeGameMessageMapper = ticTacToeGameMessageMapper;
        this.gameTransactionMapper = gameTransactionMapper;
        this.gameServiceClient = gameServiceClient;
        this.grpcGameTransactionClient = grpcGameTransactionClient;
    }


    @Override
    protected void handleMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.isBlank()) {
            log.warn("Received empty message from session {}", session.getId());

            return;
        }

        TicTacToeGameMessage ticTacToeGameMessage = messageDeserializer.deserialize(payload, TicTacToeGameMessage.class);
        UUID roomId = WebSocketUtil.getRoomId(session);
        UserInternalResponse user = WebSocketUtil.getUser(session);

        if (ticTacToeGameMessage.event() == null) {
            return;
        }

        log.info("Received game message: {}", ticTacToeGameMessage);

        switch (ticTacToeGameMessage.event()) {
            case READY -> handlePlayerReady(roomId, user);

            case MOVE -> handleGameMove(ticTacToeGameMessage, roomId, user);

            case BET -> handlePlayerBet(ticTacToeGameMessage, roomId, user);

            default -> log.warn("Unhandled tic tac toe event: {}", ticTacToeGameMessage.event());
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

        log.info("Player= \"{}\" is ready", user.username());

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

            log.info("Starting game in room {} with players: {}", roomId, players);

            TicTacToeGameMessage startGameRequest = ticTacToeGameMessageMapper.toGameStartMessage(
                    MessageType.SYSTEM,
                    TicTacToeGameEvent.START,
                    roomId,
                    players
            );

            TicTacToeGameMessage startGameResponse = gameServiceClient.startGame(startGameRequest);

            roomManager.broadcast(roomId, startGameResponse);
            roomManager.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);
        } catch (IllegalStateException e) {
            log.warn("Cannot start game in room {}: {}", roomId, e.getMessage());

            roomManager.removeReadyPlayers(roomId);

            roomManager.broadcast(roomId, ticTacToeGameMessageMapper.toResponse(
                    MessageType.SYSTEM,
                    TicTacToeGameEvent.START_FAILED,
                    null,
                    null,
                    roomId,
                    e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to start game in room {}: {}", roomId, e.getMessage(), e);

            roomManager.removeReadyPlayers(roomId);

            roomManager.broadcast(roomId, ticTacToeGameMessageMapper.toResponse(
                    MessageType.SYSTEM,
                    TicTacToeGameEvent.START_FAILED,
                    null,
                    null,
                    roomId,
                    "Game service unavailable, please try again"
            ));
        }
    }

    private void handleGameMove(TicTacToeGameMessage ticTacToeGameMessage, UUID roomId, UserInternalResponse user) {
        Map<UUID, String> players = roomManager.getPlayersInRoom(roomId).stream()
                .collect(Collectors.toMap(
                        ClientSession::getGuid,
                        ClientSession::getUsername
                ));

        TicTacToeGameMessage moveGameRequest = ticTacToeGameMessageMapper.toGameMoveMessage(
                MessageType.SYSTEM,
                ticTacToeGameMessage.event(),
                user.guid(),
                roomId,
                ticTacToeGameMessage.board(),
                ticTacToeGameMessage.cell(),
                ticTacToeGameMessage.currentPlayerSymbol(),
                ticTacToeGameMessage.playersSymbols(),
                players
        );

        TicTacToeGameMessage moveGameResponse = gameServiceClient.processMove(moveGameRequest);

        TicTacToeGameEvent event = moveGameResponse.event();

        if (TicTacToeGameEvent.WINNER_X.equals(event)
                || TicTacToeGameEvent.WINNER_O.equals(event)
                || TicTacToeGameEvent.DRAW.equals(event)) {
            processEndGame(roomId, moveGameResponse);
        } else {
            roomManager.broadcast(roomId, moveGameResponse);
        }
    }

    private void processEndGame(UUID roomId, TicTacToeGameMessage moveGameResponse) {
        roomManager.broadcast(roomId, moveGameResponse);

        try {
            if (!TicTacToeGameEvent.DRAW.equals(moveGameResponse.event())) {
                List<PlayerBet> bets = roomManager.getPlayerBets(roomId);

                TicTacToeTransactionRequest transactionRequest = gameTransactionMapper.toTicTacToeRequest(
                        roomId,
                        roomManager.getRoomType(),
                        bets,
                        moveGameResponse.winner()
                );

                GameTransactionResponse transactionResponse = grpcGameTransactionClient.saveTicTacToeGameResults(transactionRequest);

                log.info("Bank service response: transactions={}", transactionResponse.getTransactionCount());
            }
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

    private void handlePlayerBet(TicTacToeGameMessage ticTacToeGameMessage, UUID roomId, UserInternalResponse user) {
        try {
            roomManager.markPlayerBet(roomId, user, ticTacToeGameMessage.bet());
        } catch (IllegalArgumentException e) {
            log.warn("Bet rejected for user={} in room={}: {}", user.username(), roomId, e.getMessage());

            ClientSession client = sessionManager.getByGuid(user.guid());

            if (client != null) {
                sessionManager.sendToSession(client, ticTacToeGameMessageMapper.toResponse(
                        MessageType.SYSTEM,
                        TicTacToeGameEvent.BET_REJECT,
                        null,
                        user.guid(),
                        roomId,
                        e.getMessage()
                ));
            }
        }
    }
}
