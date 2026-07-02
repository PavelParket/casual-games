package com.websocket_hub.manager;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.MahjongGameMessage;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.MahjongGameEvent;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.PlayerBetFactory;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.helper.WebSocketHelper;
import com.websocket_hub.mapper.MahjongGameMessageMapper;
import com.websocket_hub.mapper.MessageMapper;
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
public class MahjongGameRoomManager extends AbstractRoomManager {

    private final Map<UUID, Set<UUID>> readyPlayers = new ConcurrentHashMap<>();

    private final Map<UUID, List<PlayerBet>> playerBets = new ConcurrentHashMap<>();

    private final Map<UUID, Set<UUID>> deadlockedPlayers = new ConcurrentHashMap<>();

    private final MahjongGameMessageMapper mahjongGameMessageMapper;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final WebSocketHelper webSocketHelper;

    public MahjongGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            SessionManager sessionManager,
            RoomValidator roomValidator,
            RoomRedisRepository roomRedisRepository,
            MahjongGameMessageMapper mahjongGameMessageMapper,
            PlayerBetFactory playerBetFactory,
            PlayerBetValidator playerBetValidator,
            WebSocketHelper webSocketHelper
    ) {
        super(serializer, roomFactory, sessionManager, roomValidator, roomRedisRepository);
        this.mahjongGameMessageMapper = mahjongGameMessageMapper;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.webSocketHelper = webSocketHelper;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.MAHJONG;
    }

    @Override
    public MessageMapper getMapper() {
        return this.mahjongGameMessageMapper;
    }

    @Override
    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.MAHJONG_ROOM;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        broadcast(room.getId(), mahjongGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                MahjongGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        readyPlayers.computeIfPresent(room.getId(), (key, players) -> {
            players.remove(user.guid());
            return players.isEmpty() ? null : players;
        });

        playerBets.computeIfPresent(room.getId(), (key, bets) -> {
            bets.removeIf(bet -> bet.getGuid().equals(user.guid()));
            return bets.isEmpty() ? null : bets;
        });

        broadcast(room.getId(), mahjongGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                MahjongGameEvent.LEAVE,
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
        deadlockedPlayers.remove(roomId);
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
            ClientSession client = getClientSessionByGuid(user.guid());
            webSocketHelper.notifyBetRequired(roomId, client, MahjongGameEvent.BET_REQUIRED);
            return;
        }

        Set<UUID> ready = readyPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        ready.add(user.guid());

        broadcast(room.getId(), mahjongGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                MahjongGameEvent.READY,
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
    }

    private void removeReadyPlayer(UUID roomId, UUID playerGuid) {
        readyPlayers.computeIfPresent(roomId, (key, players) -> {
            boolean removed = players.remove(playerGuid);

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
                webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, players, MahjongGameEvent.BET, bet);

                return;
            }

            PlayerBet existingBet = bets.getFirst();
            ClientSession existingClient = getClientSessionByGuid(existingBet.getGuid());

            int compareBets = newPlayerBet.getBet().compareTo(existingBet.getBet());

            if (compareBets < 0) {
                webSocketHelper.notifyBetRejected(roomId, newClient, MahjongGameEvent.BET_REJECT, newPlayerBet.getBet().toString());

                return;
            }

            if (compareBets > 0) {
                bets.clear();
                bets.add(newPlayerBet);

                webSocketHelper.notifyBetAccepted(roomId, newClient, MahjongGameEvent.BET, bet);
                webSocketHelper.notifyBetOutbid(roomId, existingClient, MahjongGameEvent.BET_OUTBID, newPlayerBet.getBet());

                removeReadyPlayer(roomId, existingClient.getGuid());

                return;
            }

            bets.add(newPlayerBet);
            webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, players, MahjongGameEvent.BET, bet);
        }
    }

    public void removePlayerBets(UUID roomId) {
        playerBets.remove(roomId);
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

    public void broadcastMove(UUID roomId,
                              UUID toUserGuid,
                              MahjongGameMessage moverMessage,
                              MahjongGameMessage othersMessage) {
        Set<ClientSession> players = getPlayersInRoom(roomId);

        if (players.isEmpty()) {
            return;
        }

        List<Thread> threads = new ArrayList<>();

        players.forEach(client -> {
            MahjongGameMessage message = client.getGuid().equals(toUserGuid) ? moverMessage : othersMessage;
            Thread thread = Thread.ofVirtual().start(() -> sendToClient(client, message));
            threads.add(thread);
        });

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("broadcastMove interrupted for room {}", roomId);
            }
        }
    }

    public void markDeadlocked(UUID roomId, UUID playerGuid) {
        deadlockedPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(playerGuid);
    }

    public boolean isDeadlocked(UUID roomId, UUID playerGuid) {
        return deadlockedPlayers.getOrDefault(roomId, Set.of()).contains(playerGuid);
    }

    public boolean bothDeadlocked(UUID roomId) {
        Set<UUID> deadlocked = deadlockedPlayers.getOrDefault(roomId, Set.of());
        return deadlocked.size() == getPlayersInRoom(roomId).size() && !deadlocked.isEmpty();
    }
}
