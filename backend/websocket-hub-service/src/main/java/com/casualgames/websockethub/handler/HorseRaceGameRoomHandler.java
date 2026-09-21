package com.casualgames.websockethub.handler;

import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import com.casualgames.websockethub.client.GameServiceClient;
import com.casualgames.websockethub.domain.dto.client.HorseRaceGameInternalRequest;
import com.casualgames.websockethub.domain.dto.client.HorseRaceGameInternalResponse;
import com.casualgames.websockethub.domain.dto.client.UserInternalResponse;
import com.casualgames.websockethub.domain.dto.event.CountdownExpiredEvent;
import com.casualgames.websockethub.domain.dto.message.HorseRaceGameMessage;
import com.casualgames.websockethub.domain.entity.HorseRaceGamePreset;
import com.casualgames.websockethub.domain.entity.HorseRacePlayerBet;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.RoomStatus;
import com.casualgames.websockethub.domain.enums.events.HorseRaceEvent;
import com.casualgames.websockethub.manager.HorseRaceGameRoomManager;
import com.casualgames.websockethub.manager.SessionManager;
import com.casualgames.websockethub.mapper.DefaultMessageMapper;
import com.casualgames.websockethub.mapper.GameTransactionMapper;
import com.casualgames.websockethub.mapper.HorseRaceGameMessageMapper;
import com.casualgames.websockethub.serializer.MessageDeserializer;
import com.casualgames.websockethub.service.grpc.client.GrpcGameTransactionClient;
import com.casualgames.websockethub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HorseRaceGameRoomHandler extends AppWebSocketHandler<HorseRaceGameRoomManager> {

    private final HorseRaceGameMessageMapper horseRaceMessageMapper;

    private final GameTransactionMapper gameTransactionMapper;

    private final GameServiceClient gameServiceClient;

    private final GrpcGameTransactionClient grpcGameTransactionClient;

    public HorseRaceGameRoomHandler(
            SessionManager sessionManager,
            HorseRaceGameRoomManager roomManager,
            WebSocketErrorHandler errorHandler,
            MessageDeserializer messageDeserializer,
            DefaultMessageMapper defaultMessageMapper,
            HorseRaceGameMessageMapper horseRaceMessageMapper,
            GameTransactionMapper gameTransactionMapper,
            GameServiceClient gameServiceClient,
            GrpcGameTransactionClient grpcGameTransactionClient
    ) {
        super(sessionManager, roomManager, errorHandler, messageDeserializer, defaultMessageMapper);
        this.horseRaceMessageMapper = horseRaceMessageMapper;
        this.gameServiceClient = gameServiceClient;
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

        try {
            HorseRaceGameMessage horseRaceGameMessage = messageDeserializer.deserialize(payload, HorseRaceGameMessage.class);
            UUID roomId = WebSocketUtil.getRoomId(session);
            UserInternalResponse user = WebSocketUtil.getUser(session);

            log.info("Received horse race message: event={}, room={}, user={}", horseRaceGameMessage.event(), roomId, user.username());

            switch (horseRaceGameMessage.event()) {
                case BET -> handleBet(horseRaceGameMessage, roomId, user);

                case READY -> handleReady(roomId, user);

                default -> log.warn("Unhandled horse race event: {}", horseRaceGameMessage.event());
            }
        } catch (Exception e) {
            log.error("Failed to handle horse race message", e);
        }
    }

    @Override
    protected void onJoin(UUID roomId, UserInternalResponse user) {

    }

    @Override
    protected void onLeave(UUID roomId, UserInternalResponse user) {

    }

    @EventListener
    public void onCountdownExpired(CountdownExpiredEvent event) {
        UUID roomId = event.roomId();

        log.info("Countdown expired for room={}", roomId);

        if (roomManager.getPlayersInRoom(roomId).isEmpty()) {
            log.warn("Countdown expired for room={} but room is empty — skipping race start", roomId);
            return;
        }

        if (!roomManager.hasAnyBets(roomId)) {
            log.warn("Countdown expired for room={} but no bets placed — restarting countdown", roomId);
            restartCountdown(roomId);
            return;
        }

        HorseRaceGamePreset preset = roomManager.getPreset(roomId);

        if (preset == null) {
            log.error("Countdown expired for room={} but preset not found — cannot start race", roomId);
            return;
        }

        startRace(roomId, preset);
        roomManager.removeReadyPlayers(roomId);
    }

    private void restartCountdown(UUID roomId) {
        roomManager.restartCountdown(roomId);
    }

    private void handleBet(HorseRaceGameMessage message, UUID roomId, UserInternalResponse user) {
        roomManager.placeBet(roomId, user, message.horseIndex(), message.bet());
    }

    private void handleReady(UUID roomId, UserInternalResponse user) {
        try {
            HorseRaceGamePreset preset = roomManager.getPreset(roomId);

            if (preset == null) {
                log.warn("Player {} sent READY but preset not found for room={}", user.username(), roomId);
                return;
            }

            roomManager.markReady(roomId, user);

            if (roomManager.areAllPlayersReady(roomId)) {
                startRace(roomId, preset);
                roomManager.removeReadyPlayers(roomId);
            }
        } catch (Exception e) {
            log.error("Failed to handle READY for room={}, user={}", roomId, user.username(), e);
        }
    }

    private void startRace(UUID roomId, HorseRaceGamePreset horseRaceGamePreset) {
        try {
            Map<UUID, String> participants = roomManager.getParticipants(roomId);

            Map<UUID, Integer> players = roomManager.getPlayerBets(roomId).stream()
                    .collect(Collectors.toMap(
                            HorseRacePlayerBet::getGuid,
                            HorseRacePlayerBet::getHorseIndex
                    ));

            HorseRaceGameInternalRequest startRequest = horseRaceMessageMapper.toStartRequest(
                    HorseRaceEvent.START,
                    roomId,
                    players,
                    horseRaceGamePreset.horseCount()
            );

            HorseRaceGameInternalResponse startResponse = gameServiceClient.startRace(startRequest)
                    .orElseThrow(() -> new RuntimeException("Empty start response from game-service"));

            HorseRaceGameMessage horseRaceGameMessage = horseRaceMessageMapper.toMessage(
                    startResponse,
                    MessageType.SYSTEM,
                    HorseRaceEvent.START,
                    null,
                    null,
                    "Race started",
                    participants
            );

            roomManager.broadcast(roomId, horseRaceGameMessage);

            roomManager.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);

            log.info("Race started and broadcasted for room={}: winner=horse#{}", roomId, startResponse.winnerHorseIndex());

            processGameEnd(roomId, startResponse.winnerHorseIndex());
        } catch (Exception e) {
            log.error("Failed to start race for room={}", roomId, e);
        }
    }

    private void processGameEnd(UUID roomId, Integer winnerHorseIndex) {
        try {
            HorseRaceGameInternalRequest resultRequest = horseRaceMessageMapper.toFinishRequest(
                    HorseRaceEvent.RESULT,
                    roomId
            );

            gameServiceClient.finishRace(resultRequest);

            sendTransactions(roomId, winnerHorseIndex);

            log.info("Race result sent to game-service for room={}", roomId);
        } catch (Exception e) {
            log.error("Failed to notify result for room={}", roomId, e);
        } finally {
            roomManager.removePlayerBets(roomId);
            roomManager.updateRoomStatus(roomId, RoomStatus.FINISHED);
        }
    }

    private void sendTransactions(UUID roomId, Integer winnerHorseIndex) {
        try {
            Collection<HorseRacePlayerBet> bets = roomManager.getPlayerBets(roomId);

            if (bets.isEmpty()) {
                log.info("No bets to process for room={}", roomId);
                return;
            }

            HorseRaceTransactionRequest request = gameTransactionMapper.toHorseRaceRequest(
                    roomId,
                    roomManager.getRoomType(),
                    winnerHorseIndex,
                    bets
            );

            GameTransactionResponse response = grpcGameTransactionClient.saveHorseRaceGameResults(request);

            log.info("Bank-service response for room={}: transactions={}", roomId, response.getTransactionCount());
        } catch (Exception e) {
            log.error("Failed to process transactions for room={}", roomId, e);
        }
    }
}
