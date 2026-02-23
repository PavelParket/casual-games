package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.TicTacToeGameEvent;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.PlayerBetFactory;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.helper.WebSocketHelper;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.mapper.TicTacToeGameMessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
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

    private final WebSocketHelper webSocketHelper;

    public TicTacToeGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            SessionManager sessionManager,
            RoomValidator roomValidator,
            RoomRedisRepository roomRedisRepository,
            TicTacToeGameMessageMapper ticTacToeGameMessageMapper,
            PlayerBetFactory playerBetFactory,
            PlayerBetValidator playerBetValidator,
            WebSocketHelper webSocketHelper
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator, roomRedisRepository);
        this.messageMapper = ticTacToeGameMessageMapper;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.webSocketHelper = webSocketHelper;
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
    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.TIC_TAC_TOE_ROOM;
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

        playerBets.computeIfPresent(room.getId(), (key, bets) -> {
            bets.removeIf(bet -> bet.getGuid().equals(user.guid()));

            return bets.isEmpty() ? null : bets;
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
    protected void onCreateRoom(Room room) {

    }

    @Override
    protected void onDeleteRoom(UUID roomId) {

    }

    @Override
    public Integer getReadyPlayerCount(UUID roomId) {
        return readyPlayers.getOrDefault(roomId, Set.of()).size();
    }

    public void markReady(UUID roomId, UserInternalResponse user) {
        Room room = getRoomsMap().getOrDefault(roomId, null);

        if (room == null) {
            throw new IllegalArgumentException("Room id=" + roomId + " not found!");
        }

        List<PlayerBet> bets = playerBets.getOrDefault(roomId, List.of());
        if (!playerBetValidator.hasPlayerPlacedBet(bets, user.guid())) {
            log.warn("Player {} tried to ready without placing a bet", user.username());

            ClientSession client = getClientSessionByGuid(user.guid());
            webSocketHelper.notifyBetRequired(roomId, client, TicTacToeGameEvent.BET_REQUIRED);
            return;
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
        Set<ClientSession> players = getPlayersInRoom(roomId);

        return ready != null && ready.size() == 2 && players.size() == 2
                && ready.containsAll(players.stream().map(ClientSession::getGuid).collect(Collectors.toSet()));
    }

    public void removeReadyPlayers(UUID roomId) {
        readyPlayers.remove(roomId);

        log.info("Cleared ready players for room {}", roomId);
    }

    private void removeReadyPlayer(UUID roomId, UUID playerGuid) {
        readyPlayers.computeIfPresent(roomId, (key, players) -> {
            boolean removed = players.remove(playerGuid);

            if (removed) {
                log.info("Removed ready status for player {} in room {} (bet was outbid)", playerGuid, roomId);
            }

            return players.isEmpty() ? null : players;
        });
    }

    public void markPlayerBet(UUID roomId, UserInternalResponse user, BigDecimal bet) {
        PlayerBet newPlayerBet = playerBetFactory.create(user.guid(), bet, user.balance());
        playerBetValidator.validateBet(newPlayerBet);

        ClientSession newClient = getClientSessionByGuid(user.guid());
        List<PlayerBet> bets = playerBets.computeIfAbsent(roomId, key -> new ArrayList<>());

        Set<ClientSession> players = getPlayersInRoom(roomId).stream()
                .filter(player -> !player.getGuid().equals(newClient.getGuid()))
                .collect(Collectors.toSet());

        synchronized (bets) {
            bets.removeIf(playerBet -> playerBet.getGuid().equals(user.guid()));

            if (bets.isEmpty()) {
                bets.add(newPlayerBet);
                webSocketHelper.notifyBetAccepted(roomId, newClient, TicTacToeGameEvent.BET, bet);
                log.info("First bet in room {} by player {}: {}", roomId, user.username(), bet);
                return;
            }

            PlayerBet existingBet = bets.getFirst();
            ClientSession existingClient = getClientSessionByGuid(existingBet.getGuid());

            int compareBets = newPlayerBet.getBet().compareTo(existingBet.getBet());

            if (compareBets < 0) {
                webSocketHelper.notifyBetRejected(roomId, newClient, TicTacToeGameEvent.BET_REJECT, newPlayerBet.getBet().toString());
                log.info("Bet rejected in room {} for player {}: {} (existing: {})", roomId, user.username(), bet, existingBet.getBet());
                return;
            }

            if (compareBets > 0) {
                bets.clear();
                bets.add(newPlayerBet);

                webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, players, TicTacToeGameEvent.BET, bet);
                webSocketHelper.notifyBetOutbid(roomId, existingClient, TicTacToeGameEvent.BET_OUTBID, newPlayerBet.getBet());

                removeReadyPlayer(roomId, existingClient.getGuid());

                log.info("Bet accepted (outbid) in room {} by player {}: {} (outbid player: {}, ready status reset)", roomId, user.username(), bet, existingClient.getUsername());

                return;
            }

            bets.add(newPlayerBet);
            webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, players, TicTacToeGameEvent.BET, bet);

            log.info("Bet accepted (equal) in room {} by player {}: {} (both players ready to start)", roomId, user.username(), bet);
        }
    }

    public void removePlayerBets(UUID roomId) {
        playerBets.remove(roomId);

        log.info("Cleared players bets in room {}", roomId);
    }

    public List<PlayerBet> getPlayerBets(UUID roomId) {
        return new ArrayList<>(playerBets.getOrDefault(roomId, List.of()));
    }

    public PlayerBet getPlayerBet(UUID roomId, UUID playerGuid) {
        List<PlayerBet> bets = getPlayerBets(roomId);

        if (bets == null || bets.isEmpty()) {
            return null;
        }

        return bets.stream()
                .filter(bet -> bet.getGuid().equals(playerGuid))
                .findFirst()
                .map(playerBet -> new PlayerBet(playerBet.getGuid(), playerBet.getBet(), null))
                .orElse(null);
    }

    public void validateBetsForGameStart(UUID roomId) {
        List<PlayerBet> bets = getPlayerBets(roomId);
        playerBetValidator.validateBetsForGameStart(bets);
    }
}
