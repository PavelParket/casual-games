package com.websocket_hub.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.GameMessage;
import com.websocket_hub.manager.GameRoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class GameRoomHandler extends AppWebSocketHandler<GameRoomManager> {

    private final ObjectMapper objectMapper;

    private final GameServiceClient client;

    public GameRoomHandler(SessionManager sessionManager, GameRoomManager roomManager, ObjectMapper objectMapper, GameServiceClient client) {
        super(sessionManager, roomManager);
        this.objectMapper = objectMapper;
        this.client = client;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received game message: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            String type = (String) data.get("type");
            String roomId = WebSocketUtil.getRoomName(session);
            String userId = WebSocketUtil.getUserId(session);

            switch (type) {
                case "ready" -> handlePlayerReady(roomId, userId);
                case "move" -> handleGameMove(roomId, userId, data);
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

        if (roomManager.areBothPlayersReady(roomId)) {
            startGame(roomId);
        }
    }

    private void startGame(String roomId) {
        try {
            Set<String> players = roomManager.getUserIds(roomId);

            log.info("Starting game in room {} with players: {}", roomId, players);

            Map<String, Object> startRequest = Map.of(
                    "type", "start",
                    "roomName", roomId,
                    "players", players
            );

            Map<String, Object> gameResponse = client.startGame(startRequest);

            GameMessage message = buildGameMessage(roomId, gameResponse);
            roomManager.broadcastGameMessage(roomId, message);

        } catch (Exception e) {
            log.error("Failed to start game in room {}", roomId, e);
        }
    }

    private void handleGameMove(String roomId, String userId, Map<String, Object> data) {
        try {
            Integer cell = (Integer) data.get("cell");
            String player = (String) data.get("player");

            Object boardObj = data.get("board");
            String[][] board = convertToBoard(boardObj);

            Map<String, Object> moveRequest = Map.of(
                    "type", "move",
                    "roomName", roomId,
                    "board", board,
                    "cell", cell,
                    "player", player
            );

            Map<String, Object> gameResponse = client.processMove(moveRequest);

            GameMessage message = buildGameMessage(roomId, gameResponse);
            roomManager.broadcastGameMessage(roomId, message);

        } catch (Exception e) {
            log.error("Failed to process move in room {}", roomId, e);
        }
    }

    private GameMessage buildGameMessage(String roomId, Map<String, Object> gameResponse) {
        String type = (String) gameResponse.get("type");
        String nextPlayer = (String) gameResponse.get("nextPlayer");
        String winner = (String) gameResponse.get("winner");
        String message = (String) gameResponse.get("message");

        Object boardObj = gameResponse.get("board");
        String[][] board = convertToBoard(boardObj);

        @SuppressWarnings("unchecked")
        Map<String, String> playersSymbols = (Map<String, String>) gameResponse.get("playersSymbols");

        @SuppressWarnings("unchecked")
        Set<String> players = (Set<String>) gameResponse.get("players");

        Integer cell = gameResponse.get("cell") != null ? (Integer) gameResponse.get("cell") : null;
        String player = (String) gameResponse.get("player");

        return GameMessage.builder()
                .type(type)
                .fromUserId("system")
                .toUserId("")
                .roomId(roomId)
                .board(board)
                .cell(cell)
                .player(player)
                .nextPlayer(nextPlayer)
                .playersSymbols(playersSymbols)
                .players(players)
                .winner(winner)
                .message(message)
                .build();
    }

    private String[][] convertToBoard(Object boardObj) {
        if (boardObj == null) {
            return new String[3][3];
        }

        String[][] board = new String[3][3];

        if (boardObj instanceof Object[][] objBoard) {
            for (int i = 0; i < 3 && i < objBoard.length; i++) {
                for (int j = 0; j < 3 && j < objBoard[i].length; j++) {
                    board[i][j] = objBoard[i][j] != null ? objBoard[i][j].toString() : null;
                }
            }
        }

        return board;
    }
}
