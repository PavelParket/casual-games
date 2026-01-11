package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.SystemEvent;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.service.PlayerBetService;
import com.websocket_hub.validator.PlayerBetValidator;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TicTacToeGameRoomManager extends AbstractRoomManager {

    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();

    private final Map<String, List<PlayerBet>> playerBets = new ConcurrentHashMap<>();

    private final MessageMapper mapper;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final PlayerBetService playerBetService;

    public TicTacToeGameRoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> factory,
            SessionManager sessionManager,
            RoomValidator validator,
            TicTacToeGameMessageMapper mapper,
            ObjectFactory<PlayerBet> playerBetFactory, PlayerBetValidator playerBetValidator, PlayerBetService playerBetService
    ) {
        super(serializer, factory, sessionManager, validator);
        this.mapper = mapper;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.playerBetService = playerBetService;
    }

    @Override
    public String getName() {
        return "TicTacToeGameRoomManager";
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

    @Override
    public Integer getReadyPlayerCount(String roomName) {
        return readyPlayers.getOrDefault(roomName, Set.of()).size();
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

    public void markPlayerBet(String roomName, UserInfoInternalResponse user, BigDecimal bet) {
        PlayerBet newPlayerBet = playerBetFactory.create(user.guid(), bet, user.balance());

        playerBetValidator.validateBet(newPlayerBet);

        ClientSession newClient = getClientSessionByGuid(user.guid());
        List<PlayerBet> bets = playerBets.computeIfAbsent(roomName, key -> new ArrayList<>());

        synchronized (bets) {
            bets.removeIf(playerBet -> playerBet.getGuid().equals(user.guid()));

            if (bets.isEmpty()) {
                bets.add(newPlayerBet);
                playerBetService.notifyBetAccepted(roomName, newClient, bet, this);
                return;
            }

            if (bets.size() == 1) {
                PlayerBet oldPlayerBet = bets.getFirst();
                ClientSession oldClient = getClientSessionByGuid(oldPlayerBet.getGuid());

                if (newPlayerBet.getBet().compareTo(oldPlayerBet.getBet()) < 0) {
                    playerBetService.notifyBetRejected(roomName, newClient, newPlayerBet.getBet(), this);
                    return;
                } else if (newPlayerBet.getBet().compareTo(oldPlayerBet.getBet()) > 0) {
                    bets.clear();
                    bets.add(newPlayerBet);

                    playerBetService.notifyOutbid(roomName, oldClient, newPlayerBet.getBet(), this);
                    playerBetService.notifyBetAccepted(roomName, newClient, bet, this);

                    return;
                }

                bets.add(newPlayerBet);
                playerBetService.notifyBetAccepted(roomName, newClient, bet, this);
            }

            playerBetService.notifyBetRejected(roomName, newClient, null, this);
        }
    }

    public void clearPlayerBets(String roomName) {
        playerBets.remove(roomName);

        log.info("Cleared players bets in room {}", roomName);
    }
}
