package com.websocket_hub.handler;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.GameMessage;
import com.websocket_hub.enums.GameEvent;
import com.websocket_hub.manager.GameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.GameMessageMapper;
import com.websocket_hub.serializer.JsonDeserializer;
import com.websocket_hub.serializer.MessageDeserializer;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class GameRoomHandler extends AppWebSocketHandler<GameRoomManager> {

    private final MessageDeserializer deserializer;

    private final GameMessageMapper mapper;

    private final GameServiceClient client;

    public GameRoomHandler(
            SessionManager sessionManager,
            GameRoomManager roomManager,
            JsonDeserializer deserializer,
            GameMessageMapper mapper,
            GameServiceClient client
    ) {
        super(sessionManager, roomManager);
        this.deserializer = deserializer;
        this.mapper = mapper;
        this.client = client;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().isEmpty()) {
            log.warn("Received empty message from session {}", session.getId());

            return;
        }

        String payload = message.getPayload();

        log.debug("Received game message: {}", payload);

        try {
            GameMessage gameMessage = deserializer.deserialize(payload, GameMessage.class);

            String type = gameMessage.type();
            String roomId = WebSocketUtil.getRoomName(session);
            String userId = WebSocketUtil.getUserId(session);

            switch (type) {
                case "ready" -> handlePlayerReady(roomId, userId);
                case "move" -> handleGameMove(roomId, gameMessage);
                default -> log.warn("Unknown game message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to handle game message", e);
        }
    }

    @Override
    protected void onJoin(String roomName, String username) {

    }

    @Override
    protected void onLeave(String roomName, String username) {

    }

    private void handlePlayerReady(String roomId, String userId) {
        roomManager.markReady(roomId, userId);

        log.info("Player= \"{}\" is ready", userId);

        if (roomManager.areBothPlayersReady(roomId)) {
            startGame(roomId);
        }
    }

    private void startGame(String roomId) {
        try {
            Set<String> players = roomManager.getUserIds(roomId);

            log.info("Starting game in room {} with players: {}", roomId, players);

            GameMessage startRequest = mapper.toGameStartMessageFromParams(GameEvent.START, roomId, players);

            Optional<GameMessage> startResponse = client.startGame(startRequest);

            roomManager.broadcastGameMessage(startResponse.orElseThrow(() -> new RuntimeException("Empty state")));
        } catch (Exception e) {
            log.error("Failed to start game in room {}", roomId, e);
        }
    }

    private void handleGameMove(String roomId, GameMessage message) {
        try {
            GameMessage moveRequest = mapper.toGameMoveMessage(GameEvent.MOVE, roomId, message.board(), message.cell(), message.player());

            Optional<GameMessage> moveResponse = client.processMove(moveRequest);

            roomManager.broadcastGameMessage(moveResponse.orElseThrow(() -> new RuntimeException(("Empty state"))));
        } catch (Exception e) {
            log.error("Failed to process move in room {}", roomId, e);
        }
    }
}
