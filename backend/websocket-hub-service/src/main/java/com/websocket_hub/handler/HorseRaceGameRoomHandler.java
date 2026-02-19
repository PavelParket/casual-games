package com.websocket_hub.handler;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalRequest;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.HorseRaceGameMessage;
import com.websocket_hub.domain.entity.HorseRaceGamePreset;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import com.websocket_hub.manager.HorseRaceGameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.HorseRaceGameMessageMapper;
import com.websocket_hub.serializer.MessageDeserializer;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class HorseRaceGameRoomHandler extends AppWebSocketHandler<HorseRaceGameRoomManager> {

    private final MessageDeserializer messageDeserializer;

    private final HorseRaceGameMessageMapper horseRaceMessageMapper;

    private final GameServiceClient gameServiceClient;

    public HorseRaceGameRoomHandler(
            SessionManager sessionManager,
            HorseRaceGameRoomManager roomManager,
            MessageDeserializer messageDeserializer,
            HorseRaceGameMessageMapper horseRaceMessageMapper,
            GameServiceClient gameServiceClient
    ) {
        super(sessionManager, roomManager);
        this.messageDeserializer = messageDeserializer;
        this.horseRaceMessageMapper = horseRaceMessageMapper;
        this.gameServiceClient = gameServiceClient;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
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

            HorseRaceGameInternalRequest startRequest = horseRaceMessageMapper.toStartRequest(
                    HorseRaceEvent.START,
                    roomId,
                    participants,
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

            log.info("Race started and broadcasted for room={}: winner=horse#{}", roomId, startResponse.winnerHorseIndex());

            notifyResult(roomId);
        } catch (Exception e) {
            log.error("Failed to start race for room={}", roomId, e);
        }
    }

    private void notifyResult(UUID roomId) {
        try {
            HorseRaceGameInternalRequest resultRequest = horseRaceMessageMapper.toFinishRequest(
                    HorseRaceEvent.RESULT,
                    roomId
            );

            gameServiceClient.finishRace(resultRequest);

            roomManager.removePreset(roomId);

            log.info("Race result sent to game-service for room={}", roomId);
        } catch (Exception e) {
            log.error("Failed to notify result for room={}", roomId, e);
        }
    }
}
