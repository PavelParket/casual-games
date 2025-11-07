package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.GameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.enums.GameEvent;
import com.websocket_hub.enums.MessageType;
import com.websocket_hub.enums.SystemEvent;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.GameMessageMapper;
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

    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();

    private final GameMessageMapper mapper;

    public GameRoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> roomFactory,
            ObjectFactory<ClientSession> clientFactory,
            GameMessageMapper mapper
    ) {
        super(serializer, roomFactory, clientFactory);
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "gameRoomManager";
    }

    @Override
    protected void onAddSession(String username, String roomName, WebSocketSession session) {
        log.info("Player {} joined game room {}", username, roomName);

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.JOIN, roomName, "Player \"" + username + "\" has joined the room \"" + roomName + "\""));
    }

    @Override
    protected void onRemoveSession(String username, String roomName, WebSocketSession session) {
        log.info("Player {} left game room {}", username, roomName);

        readyPlayers.computeIfPresent(roomName, (key, players) -> {
            players.remove(username);
            return players.isEmpty() ? null : players;
        });

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.LEAVE, roomName, "Player \"" + username + "\" has left the room \"" + roomName + "\""));
    }

    public void markReady(String roomName, String username) {
        Set<String> ready = readyPlayers.computeIfAbsent(roomName, key -> ConcurrentHashMap.newKeySet());
        ready.add(username);

        log.info("Player {} ready in room {}. Total ready: {}", username, roomName, ready.size());

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, GameEvent.READY, roomName, "Player \"" + username + "\" is ready."));
    }

    public boolean areBothPlayersReady(String roomName) {
        Set<String> ready = readyPlayers.get(roomName);
        Set<String> players = getUserIds(roomName);

        return ready != null && ready.size() == 2 && players.size() == 2;
    }

    public void broadcastGameMessage(GameMessage gameMessage) {
        try {
            GameMessage message = mapper.toGameStartMessageFromEntity(gameMessage);

            broadcast(message.roomId(), message);
        } catch (Exception e) {
            log.error("Failed to broadcast game message", e);
        }
    }
}
