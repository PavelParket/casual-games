package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import com.websocket_hub.domain.dto.user_service.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.PlayerBetFactory;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.service.PlayerBetService;
import com.websocket_hub.validator.PlayerBetValidator;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicTacToeGameRoomManager extends AbstractRoomManager {

    private final Map<UUID, Set<UUID>> readyPlayers = new ConcurrentHashMap<>();

    private final Map<UUID, List<PlayerBet>> playerBets = new ConcurrentHashMap<>();

    private final MessageMapper messageMapper;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final PlayerBetService playerBetService;

    public TicTacToeGameRoomManager(
            MessageSerializer<String> serializer,
            ObjectFactory<Room> roomFactory,
            SessionManager sessionManager,
            RoomValidator roomValidator,
            TicTacToeGameMessageMapper ticTacToeGameMessageMapper,
            PlayerBetFactory playerBetFactory,
            PlayerBetValidator playerBetValidator,
            PlayerBetService playerBetService
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator);
        this.messageMapper = ticTacToeGameMessageMapper;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.playerBetService = playerBetService;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.TIC_TAC_TOE;
    }

    @Override
    public MessageMapper getMapper() {
        return this.messageMapper;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} joined game room {}", user.email(), user.username(), room.getName());

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} left game room {}", user.email(), user.username(), room.getName());

        readyPlayers.computeIfPresent(room.getId(), (key, players) -> {
            players.remove(user.guid());
            return players.isEmpty() ? null : players;
        });

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has left the room " + room.getName()
        ));
    }

    @Override
    public Integer getReadyPlayerCount(UUID roomId) {
        return readyPlayers.getOrDefault(roomId, Set.of()).size();
    }

    public void markReady(UUID roomId, UserInternalResponse user) {
        Room room = super.getRoomsMap().getOrDefault(roomId, null);

        if (room == null) {
            throw new IllegalArgumentException("Room id=" + roomId + " not found!");
        }

        Set<UUID> ready = readyPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        ready.add(user.guid());

        log.info("Player email={} username={} ready in room {}. Total ready: {}", user.email(), user.username(), room.getName(), ready.size());

        broadcast(room.getId(), messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.READY,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " is ready"
        ));
    }

    public boolean areBothPlayersReady(UUID roomId) {
        Set<UUID> ready = readyPlayers.get(roomId);
        Set<ClientSession> players = getUsersInRoom(roomId);

        return ready != null && ready.size() == 2 && players.size() == 2
                && ready.containsAll(players.stream().map(ClientSession::getGuid).collect(Collectors.toSet()));
    }

    public void clearReadyPlayers(UUID roomId) {
        readyPlayers.remove(roomId);

        log.info("Cleared ready players for room {}", roomId);
    }

    /*public void markPlayerBet(String roomName, UserInternalResponse user, BigDecimal bet) {
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

    public void clearPlayerBets(UUID roomId) {
        playerBets.remove(roomId);

        log.info("Cleared players bets in room {}", roomId);
    }*/
}
