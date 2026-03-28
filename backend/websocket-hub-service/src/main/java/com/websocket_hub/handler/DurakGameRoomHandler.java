package com.websocket_hub.handler;

import com.websocket_hub.client.BankServiceClient;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.DurakGameInternalRequest;
import com.websocket_hub.domain.dto.client.DurakGameInternalResponse;
import com.websocket_hub.domain.dto.client.DurakPlayerViewResponse;
import com.websocket_hub.domain.dto.client.DurakTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.DurakTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DurakGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.events.DurakGameEvent;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.manager.DurakGameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.DurakGameMessageMapper;
import com.websocket_hub.mapper.DurakTransactionMapper;
import com.websocket_hub.serializer.MessageDeserializer;
import com.websocket_hub.service.scheduler.DurakTurnTimerScheduler;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DurakGameRoomHandler extends AppWebSocketHandler<DurakGameRoomManager> {

    private final MessageDeserializer messageDeserializer;

    private final DurakGameMessageMapper durakGameMessageMapper;

    private final DurakTransactionMapper durakTransactionMapper;

    private final GameServiceClient gameServiceClient;

    private final BankServiceClient bankServiceClient;

    private final DurakTurnTimerScheduler turnTimerScheduler;

    public DurakGameRoomHandler(
            SessionManager sessionManager,
            DurakGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DurakGameMessageMapper durakGameMessageMapper,
            DurakTransactionMapper durakTransactionMapper,
            GameServiceClient gameServiceClient,
            BankServiceClient bankServiceClient,
            DurakTurnTimerScheduler turnTimerScheduler
    ) {
        super(sessionManager, roomManager, errorHandler);
        this.messageDeserializer = messageDeserializer;
        this.durakGameMessageMapper = durakGameMessageMapper;
        this.durakTransactionMapper = durakTransactionMapper;
        this.gameServiceClient = gameServiceClient;
        this.bankServiceClient = bankServiceClient;
        this.turnTimerScheduler = turnTimerScheduler;
    }

    @Override
    protected void handleMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.isBlank()) {
            log.warn("Received empty message from session={}", session.getId());
            return;
        }

        DurakGameMessage durakGameMessage = messageDeserializer.deserialize(payload, DurakGameMessage.class);
        UUID roomId = WebSocketUtil.getRoomId(session);
        UserInternalResponse user = WebSocketUtil.getUser(session);

        if (durakGameMessage.event() == null) {
            return;
        }

        log.info("Received durak game message: event={}, room={}, user={}", durakGameMessage.event(), roomId, user.username());

        switch (durakGameMessage.event()) {
            case BET -> handleBet(durakGameMessage, roomId, user);

            case READY -> handleReady(roomId, user);

            case MOVE -> handleMove(durakGameMessage, roomId, user);

            default -> log.warn("Unhandled Durak event={}", durakGameMessage.event());
        }
    }

    @Override
    protected void onJoin(UUID roomId, UserInternalResponse user) {

    }

    @Override
    protected void onLeave(UUID roomId, UserInternalResponse user) {

    }

    private void handleBet(DurakGameMessage message, UUID roomId, UserInternalResponse user) {
        roomManager.markPlayerBet(roomId, user, message.bet());
    }

    private void handleReady(UUID roomId, UserInternalResponse user) {
        roomManager.markReady(roomId, user);

        if (roomManager.areBothPlayersReady(roomId)) {
            startGame(roomId);
            roomManager.removeReadyPlayers(roomId);
        }
    }

    private void startGame(UUID roomId) {
        try {
            roomManager.validateBetsForGameStart(roomId);

            List<UUID> players = roomManager.getPlayerGuids(roomId);

            DurakGameInternalRequest startRequest = durakGameMessageMapper.toStartGameRequest(roomId, players);

            DurakGameInternalResponse startResponse = gameServiceClient.startDurakGame(startRequest);

            roomManager.registerActiveGame(roomId, startResponse.id());
            roomManager.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);

            broadcastPlayerViews(roomId, startResponse.playerViews());
            broadcastTimer(roomId);

            UUID firstActor = resolveCurrentActor(startResponse.playerViews());

            turnTimerScheduler.startTimer(roomId, () -> handleTimeout(roomId, firstActor));

            log.info("Durak game started: gameId={} room={} firstActor={}", startResponse.id(), roomId, firstActor);
        } catch (GameException e) {
            roomManager.removeReadyPlayers(roomId);
            errorHandler.handle(roomId, roomManager.getPlayersInRoom(roomId), e);

        } catch (Exception e) {
            roomManager.removeReadyPlayers(roomId);
            errorHandler.handle(roomId, roomManager.getPlayersInRoom(roomId), new GameException(ErrorCode.SERVICE_UNAVAILABLE, e));
        }
    }

    private void handleMove(DurakGameMessage message, UUID roomId, UserInternalResponse user) {
        Long gameId = roomManager.getActiveGameId(roomId);

        if (gameId == null) {
            log.warn("No active game found for room={}, ignoring move from user={}", roomId, user.username());
            return;
        }

        DurakGameInternalRequest moveRequest = durakGameMessageMapper.toMoveGameRequest(
                gameId,
                roomId,
                user.guid(),
                message.action(),
                message.card()
        );

        DurakGameInternalResponse moveResponse = gameServiceClient.processDurakMove(moveRequest);

        broadcastPlayerViews(roomId, moveResponse.playerViews());

        if (Boolean.TRUE.equals(moveResponse.isGameOver())) {
            turnTimerScheduler.cancelTimer(roomId);
            processGameOver(roomId, moveResponse.winnerId());
            return;
        }

        UUID nextActor = resolveCurrentActor(moveResponse.playerViews());

        turnTimerScheduler.resetTimer(roomId, () -> handleTimeout(roomId, nextActor));

        broadcastTimer(roomId);
    }

    private void handleTimeout(UUID roomId, UUID timedOutPlayerId) {
        log.info("Turn timeout: room={} timedOutPlayer={}", roomId, timedOutPlayerId);

        UUID winnerId = roomManager.getOpponentId(roomId, timedOutPlayerId);
        Long gameId = roomManager.getActiveGameId(roomId);

        if (gameId != null) {
            try {
                gameServiceClient.processDurakEnd(durakGameMessageMapper.toEndGameRequest(gameId, winnerId));
            } catch (Exception e) {
                log.error("Failed to notify game-service of timeout: gameId={} room={}", gameId, roomId, e);
            }
        } else {
            log.warn("Cannot notify game-service of timeout — no active gameId for room={}", roomId);
        }

        processGameOver(roomId, winnerId);
    }

    private void processGameOver(UUID roomId, UUID winnerId) {
        try {
            roomManager.broadcast(roomId, durakGameMessageMapper.toGameOverMessage(
                    MessageType.SYSTEM,
                    DurakGameEvent.GAME_OVER,
                    roomId,
                    winnerId
            ));

            List<PlayerBet> playerBets = roomManager.getPlayerBets(roomId);

            DurakTransactionInternalRequest transactionRequest = durakTransactionMapper.toInternalRequest(
                    roomId,
                    roomManager.getRoomType(),
                    playerBets,
                    winnerId
            );
            DurakTransactionInternalResponse transactionResponse = bankServiceClient.sendDurakGameResults(transactionRequest);

            log.info("Bank-service response: status={}, message={}, transactions={}", transactionResponse.status(), transactionResponse.message(), transactionResponse.transactionsCreated());

        } catch (Exception e) {
            log.error("Failed to process game over for room={}", roomId, e);
        } finally {
            roomManager.removePlayerBets(roomId);
            roomManager.removeActiveGame(roomId);
            roomManager.updateRoomStatus(roomId, RoomStatus.FINISHED);
        }
    }

    private void broadcastPlayerViews(UUID roomId, List<DurakPlayerViewResponse> playerViews) {
        List<Thread> threads = new ArrayList<>();

        for (DurakPlayerViewResponse view : playerViews) {
            ClientSession client = sessionManager.getByGuid(view.playerGuid());

            if (client == null) {
                log.warn("Session not found for playerGuid={} in room={}", view.playerGuid(), roomId);
                continue;
            }

            DurakGameMessage message = durakGameMessageMapper.toGameMessage(
                    MessageType.SYSTEM,
                    DurakGameEvent.GAME_STATE,
                    view.playerGuid(),
                    roomId,
                    view
            );

            Thread thread = Thread.ofVirtual().start(() -> {
                sessionManager.sendToSession(client, message);
                log.info("Sent message: {}", message);
            });

            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while sending player views in room={}", roomId);
            }
        }
    }

    private void broadcastTimer(UUID roomId) {
        roomManager.broadcast(roomId, durakGameMessageMapper.toTimerMessage(
                MessageType.SYSTEM,
                DurakGameEvent.TIMER_UPDATE,
                roomId,
                DurakTurnTimerScheduler.TURN_SECONDS
        ));
    }

    private UUID resolveCurrentActor(List<DurakPlayerViewResponse> playerViews) {
        return playerViews.stream()
                .filter(playerView -> Boolean.TRUE.equals(playerView.isMyTurn()))
                .map(DurakPlayerViewResponse::playerGuid)
                .findFirst()
                .orElse(null);
    }
}
