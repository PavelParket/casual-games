package com.websocket_hub.handler;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.message.TicTacToeGameMessage;
import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.MessageType;
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

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TicTacToeGameRoomHandler extends AppWebSocketHandler<TicTacToeGameRoomManager> {

    private final MessageDeserializer deserializer;

    private final TicTacToeGameMessageMapper ticTacToeGameMessageMapper;

    private final GameServiceClient gameServiceClient;

    public TicTacToeGameRoomHandler(
            SessionManager sessionManager,
            TicTacToeGameRoomManager roomManager,
            JsonDeserializer deserializer,
            TicTacToeGameMessageMapper ticTacToeGameMessageMapper,
            GameServiceClient gameServiceClient
    ) {
        super(sessionManager, roomManager);
        this.deserializer = deserializer;
        this.ticTacToeGameMessageMapper = ticTacToeGameMessageMapper;
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
            TicTacToeGameMessage ticTacToeGameMessage = deserializer.deserialize(payload, TicTacToeGameMessage.class);
            UUID roomId = WebSocketUtil.getRoomId(session);
            UserInternalResponse user = WebSocketUtil.getUser(session);

            log.info("Received game message: {}", ticTacToeGameMessage);

            switch (ticTacToeGameMessage.event()) {
                case READY -> handlePlayerReady(roomId, user);

                case MOVE -> handleGameMove(ticTacToeGameMessage, roomId, user);

                case BET -> handlePlayerBet(ticTacToeGameMessage, roomId, user);

                default -> log.warn("Unknown game message event: {}", ticTacToeGameMessage.event());
            }
        } catch (Exception e) {
            log.error("Failed to handle game message", e);
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

            roomManager.clearReadyPlayers(roomId);
        }
    }

    private void startGame(UUID roomId) {
        try {
            Map<UUID, String> players = roomManager.getUsersInRoom(roomId).stream()
                    .collect(Collectors.toMap(
                            ClientSession::getGuid,
                            ClientSession::getUsername
                    ));

            if (players.isEmpty()) {
                throw new IllegalStateException("Room is empty!");
            }

            log.info("Starting game in room {} with players: {}", roomId, players);

            TicTacToeGameMessage startGameRequest = ticTacToeGameMessageMapper.toGameStartMessage(MessageType.SYSTEM, TicTacToeGameEvent.START, roomId, players);

            TicTacToeGameMessage startGameResponse = gameServiceClient.startGame(startGameRequest)
                    .orElseThrow(() -> new RuntimeException("Empty state"));

            roomManager.broadcast(roomId, startGameResponse);
        } catch (Exception e) {
            log.error("Failed to start game in room {}", roomId);
        }
    }

    private void handleGameMove(TicTacToeGameMessage ticTacToeGameMessage, UUID roomId, UserInternalResponse user) {
        try {
            Map<UUID, String> players = roomManager.getUsersInRoom(roomId).stream()
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

            TicTacToeGameMessage moveGameResponse = gameServiceClient.processMove(moveGameRequest)
                    .orElseThrow(() -> new RuntimeException(("Empty state")));

            //todo: куда-то сюда всунуть обновление баланса

            roomManager.broadcast(roomId, moveGameResponse);
        } catch (Exception e) {
            log.error("Failed to process move in room {}", roomId, e);
        }
    }

    private void handlePlayerBet(TicTacToeGameMessage ticTacToeGameMessage, UUID roomId, UserInternalResponse user) {
        roomManager.markPlayerBet(roomId, user, ticTacToeGameMessage.bet());
    }
}
