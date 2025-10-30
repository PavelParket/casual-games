package com.websocket_hub.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.domain.dto.GameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GameRoomManager extends AbstractRoomManager {

    private final ObjectMapper objectMapper;

    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();

    public GameRoomManager(MessageSerializer<String> serializer, ObjectFactory<Room> roomFactory, ObjectFactory<ClientSession> clientFactory, ObjectMapper objectMapper) {
        super(serializer, roomFactory, clientFactory);
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "gameRoomManager";
    }

    @Override
    protected void onAddSession(String username, String roomName, WebSocketSession session) {
        log.info("Player {} joined game room {}", username, roomName);
        broadcastPlayerList(roomName);
    }

    @Override
    protected void onRemoveSession(String username, String roomName, WebSocketSession session) {
        log.info("Player {} left game room {}", username, roomName);

        readyPlayers.computeIfPresent(roomName, (key, players) -> {
            players.remove(username);
            return players.isEmpty() ? null : players;
        });

        broadcastPlayerList(roomName);
    }

    public void markReady(String roomName, String username) {
        Set<String> ready = readyPlayers.computeIfAbsent(roomName, key -> ConcurrentHashMap.newKeySet());
        ready.add(username);

        log.info("Player {} ready in room {}. Total ready: {}", username, roomName, ready.size());

        broadcastPlayerList(roomName);
    }

    public boolean areBothPlayersReady(String roomName) {
        Set<String> ready = readyPlayers.get(roomName);
        Set<String> players = getUserIds(roomName);

        return ready != null && ready.size() == 2 && players.size() == 2;
    }

    public void broadcastGameMessage(String roomName, GameMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);

            GameMessage wrappedMessage = GameMessage.builder()
                    .type(message.type())
                    .fromUserId(message.fromUserId())
                    .toUserId(message.toUserId())
                    .roomId(roomName)
                    .board(message.board())
                    .cell(message.cell())
                    .player(message.player())
                    .nextPlayer(message.nextPlayer())
                    .playersSymbols(message.playersSymbols())
                    .players(message.players())
                    .winner(message.winner())
                    .message(json)
                    .build();

            broadcast(roomName, wrappedMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast game message", e);
        }
    }

    private void broadcastPlayerList(String roomName) {
        Set<String> players = getUserIds(roomName);
        Set<String> ready = readyPlayers.getOrDefault(roomName, Set.of());

        try {
            Map<String, Object> playersData = Map.of(
                    "players", players,
                    "readyPlayers", ready,
                    "totalPlayers", players.size(),
                    "readyCount", ready.size()
            );

            String content = objectMapper.writeValueAsString(playersData);

            GameMessage message = GameMessage.builder()
                    .type("player_list")
                    .fromUserId("system")
                    .toUserId("")
                    .roomId(roomName)
                    .message(content)
                    .build();

            broadcast(roomName, message);
        } catch (Exception e) {
            log.error("Failed to broadcast player list", e);
        }
    }

    public void resetRoom(String roomId) {
        readyPlayers.remove(roomId);
        log.info("Room {} reset", roomId);
    }
}
