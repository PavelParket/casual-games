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

        try {
            GameMessage gameMessage = deserializer.deserialize(payload, GameMessage.class);

            log.info("Received game message: {}", gameMessage);

            String event = gameMessage.event();
            String roomName = WebSocketUtil.getRoomName(session);
            String userId = WebSocketUtil.getUserId(session);

            switch (event) {
                case "ready" -> handlePlayerReady(roomName, userId);
                case "move" -> handleGameMove(roomName, gameMessage);
                default -> log.warn("Unknown game message event: {}", event);
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

    private void handlePlayerReady(String roomName, String userId) {
        roomManager.markReady(roomName, userId);

        log.info("Player= \"{}\" is ready", userId);

        if (roomManager.areBothPlayersReady(roomName)) {
            startGame(roomName);

            roomManager.clearReadyPlayers(roomName);
        }
    }

    private void startGame(String roomName) {
        try {
            Set<String> players = roomManager.getUserIds(roomName);

            log.info("Starting game in room {} with players: {}", roomName, players);

            GameMessage startRequest = mapper.toGameStartMessageFromParams(GameEvent.START, roomName, players);

            GameMessage startResponse = client.startGame(startRequest).orElseThrow(() -> new RuntimeException("Empty state"));

            roomManager.broadcast(startResponse.roomName(), startResponse);
        } catch (Exception e) {
            log.error("Failed to start game in room {}", roomName, e);
        }
    }

    private void handleGameMove(String roomName, GameMessage message) {
        try {
            GameMessage moveRequest = mapper.toGameMoveMessage(GameEvent.MOVE, roomName, message.board(), message.cell(), message.player());

            GameMessage moveResponse = client.processMove(moveRequest).orElseThrow(() -> new RuntimeException(("Empty state")));

            roomManager.broadcast(moveResponse.roomName(), moveResponse);
        } catch (Exception e) {
            log.error("Failed to process move in room {}", roomName, e);
        }
    }
}
