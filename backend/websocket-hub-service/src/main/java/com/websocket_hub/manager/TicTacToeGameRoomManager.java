package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.SystemEvent;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TicTacToeGameRoomManager extends AbstractRoomManager {

    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();

    private final MessageMapper mapper;

    public TicTacToeGameRoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> roomFactory,
            SessionManager sessionManager,
            TicTacToeGameMessageMapper mapper
    ) {
        super(serializer, roomFactory, sessionManager);
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "gameRoomManager";
    }

    @Override
    protected void onAddSession(UserInfoInternalResponse user, String roomName, WebSocketSession session) {
        log.info("Player {} joined game room {}", user.email(), roomName);

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.JOIN, roomName, "Player " + user.username() + " has joined the room " + roomName));
    }

    @Override
    protected void onRemoveSession(UserInfoInternalResponse user, String roomName, WebSocketSession session) {
        log.info("Player {} left game room {}", user.email(), roomName);

        readyPlayers.computeIfPresent(roomName, (key, players) -> {
            players.remove(user.email());
            return players.isEmpty() ? null : players;
        });

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, SystemEvent.LEAVE, roomName, "Player " + user.username() + " has left the room " + roomName));
    }

    public void markReady(String roomName, String email) {
        Set<String> ready = readyPlayers.computeIfAbsent(roomName, key -> ConcurrentHashMap.newKeySet());
        ready.add(email);

        log.info("Player {} ready in room {}. Total ready: {}", email, roomName, ready.size());

        broadcast(roomName, mapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.READY, roomName, "Player " + email + " is ready."));
    }

    public boolean areBothPlayersReady(String roomName) {
        Set<String> ready = readyPlayers.get(roomName);
        Set<String> players = getUserEmails(roomName);

        return ready != null && ready.size() == 2 && players.size() == 2;
    }

    public void clearReadyPlayers(String roomName) {
        readyPlayers.remove(roomName);

        log.info("Cleared ready players for room {}", roomName);
    }

    public Integer getReadyPlayerCount(String roomName) {
        return readyPlayers.getOrDefault(roomName, Set.of()).size();
    }
}
