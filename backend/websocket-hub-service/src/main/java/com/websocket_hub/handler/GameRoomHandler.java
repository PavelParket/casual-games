package com.websocket_hub.handler;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.TicTacToeGameMessage;
import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.manager.TicTacToeGameRoomManager;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
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
public class GameRoomHandler extends AppWebSocketHandler<TicTacToeGameRoomManager> {

    private final MessageDeserializer deserializer;

    private final TicTacToeGameMessageMapper mapper;

    private final GameServiceClient gameServiceClient;

    public GameRoomHandler(
            SessionManager sessionManager,
            TicTacToeGameRoomManager roomManager,
            JsonDeserializer deserializer,
            TicTacToeGameMessageMapper mapper,
            GameServiceClient gameServiceClient
    ) {
        super(sessionManager, roomManager);
        this.deserializer = deserializer;
        this.mapper = mapper;
        this.gameServiceClient = gameServiceClient;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().isEmpty()) {
            log.warn("Received empty message from session {}", session.getId());

            return;
        }

        String payload = message.getPayload();

        try {
            TicTacToeGameMessage ticTacToeGameMessage = deserializer.deserialize(payload, TicTacToeGameMessage.class);

            log.info("Received game message: {}", ticTacToeGameMessage);

            TicTacToeGameEvent event = TicTacToeGameEvent.fromDescription(ticTacToeGameMessage.event());
            String roomName = WebSocketUtil.getRoomName(session);
            UserInfoInternalResponse user = WebSocketUtil.getUser(session);

            switch (event) {
                case READY -> handlePlayerReady(roomName, user.email());
                case MOVE -> handleGameMove(roomName, ticTacToeGameMessage);
                default -> log.warn("Unknown game message event: {}", event);
            }
        } catch (Exception e) {
            log.error("Failed to handle game message", e);
        }
    }

    @Override
    protected void onJoin(String roomName, UserInfoInternalResponse user) {

    }

    @Override
    protected void onLeave(String roomName, UserInfoInternalResponse user) {

    }

    private void handlePlayerReady(String roomName, String email) {
        roomManager.markReady(roomName, email);

        log.info("Player= \"{}\" is ready", email);

        if (roomManager.areBothPlayersReady(roomName)) {
            startGame(roomName);

            roomManager.clearReadyPlayers(roomName);
        }
    }

    private void startGame(String roomName) {
        try {
            Set<String> players = roomManager.getUserEmails(roomName);

            log.info("Starting game in room {} with players: {}", roomName, players);

            TicTacToeGameMessage startRequest = mapper.toGameStartMessageFromParams(TicTacToeGameEvent.START, roomName, players);

            TicTacToeGameMessage startResponse = gameServiceClient.startGame(startRequest)
                    .orElseThrow(() -> new RuntimeException("Empty state"));

            roomManager.broadcast(startResponse.roomName(), startResponse);
        } catch (Exception e) {
            log.error("Failed to start game in room {}", roomName, e);
        }
    }

    private void handleGameMove(String roomName, TicTacToeGameMessage message) {
        try {
            TicTacToeGameMessage moveRequest = mapper.toGameMoveMessage(TicTacToeGameEvent.MOVE, roomName, message.board(), message.cell(), message.player());

            TicTacToeGameMessage moveResponse = gameServiceClient.processMove(moveRequest)
                    .orElseThrow(() -> new RuntimeException(("Empty state")));

            roomManager.broadcast(moveResponse.roomName(), moveResponse);
        } catch (Exception e) {
            log.error("Failed to process move in room {}", roomName, e);
        }
    }
}
