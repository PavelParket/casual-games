package com.websocket_hub.manager;

import com.common_utils.exception.NotFoundException;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.DurakGameEvent;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.exception.GameException;
import com.websocket_hub.factory.ObjectFactory;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.helper.WebSocketHelper;
import com.websocket_hub.mapper.DurakGameMessageMapper;
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

import static com.websocket_hub.config.ResourceMessageConstants.MUST_PLACE_BET_BEFORE_READY;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_NOT_FOUND;

@Service
@Slf4j
public class DurakGameRoomManager extends AbstractRoomManager {

    private final Map<UUID, Set<UUID>> readyPlayers = new ConcurrentHashMap<>();

    private final Map<UUID, List<PlayerBet>> playerBets = new ConcurrentHashMap<>();

    private final DurakGameMessageMapper durakGameMessageMapper;

    private final ObjectFactory<PlayerBet> playerBetFactory;

    private final PlayerBetValidator playerBetValidator;

    private final WebSocketHelper webSocketHelper;

    public DurakGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            SessionManager sessionManager,
            RoomValidator validator,
            RoomRedisRepository redisRepository,
            DurakGameMessageMapper durakGameMessageMapper,
            ObjectFactory<PlayerBet> playerBetFactory,
            PlayerBetValidator playerBetValidator,
            WebSocketHelper webSocketHelper
    ) {
        super(serializer, roomFactory, sessionManager, validator, redisRepository);
        this.durakGameMessageMapper = durakGameMessageMapper;
        this.playerBetFactory = playerBetFactory;
        this.playerBetValidator = playerBetValidator;
        this.webSocketHelper = webSocketHelper;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DURAK;
    }

    @Override
    public MessageMapper getMapper() {
        return durakGameMessageMapper;
    }

    @Override
    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.DURAK_ROOM;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} joined Durak room={}", user.email(), user.username(), room.getName());

        broadcast(room.getId(), durakGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DurakGameEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} left Durak room={}", user.email(), user.username(), room.getName());

        readyPlayers.computeIfPresent(room.getId(), (key, players) -> {
            players.remove(user.guid());
            return players.isEmpty() ? null : players;
        });

        playerBets.computeIfPresent(room.getId(), (key, bets) -> {
            bets.removeIf(bet -> bet.getGuid().equals(user.guid()));
            return bets.isEmpty() ? null : bets;
        });

        broadcast(room.getId(), durakGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DurakGameEvent.LEAVE,
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
        readyPlayers.remove(roomId);
        playerBets.remove(roomId);
    }

    @Override
    public Integer getReadyPlayerCount(UUID roomId) {
        return readyPlayers.getOrDefault(roomId, Set.of()).size();
    }

    public void markReady(UUID roomId, UserInternalResponse user) {
        Room room = getRoomsMap().get(roomId);

        if (room == null) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }

        List<PlayerBet> bets = playerBets.getOrDefault(roomId, List.of());

        if (!playerBetValidator.hasPlayerPlacedBet(bets, user.guid())) {
            throw new GameException(ErrorCode.VALIDATION_ERROR, MUST_PLACE_BET_BEFORE_READY);
        }

        Set<UUID> ready = readyPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        ready.add(user.guid());

        log.info("Player {} is ready in room={}. Total ready: {}", user.username(), roomId, ready.size());

        broadcast(roomId, durakGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                DurakGameEvent.READY,
                user.guid(),
                null,
                roomId,
                "Player " + user.username() + " is ready"
        ));
    }

    public boolean areBothPlayersReady(UUID roomId) {
        Set<UUID> ready = readyPlayers.get(roomId);
        Set<ClientSession> players = getPlayersInRoom(roomId);

        return ready != null
                && ready.size() == 2
                && players.size() == 2
                && ready.containsAll(players.stream().map(ClientSession::getGuid).collect(Collectors.toSet()));
    }

    public void removeReadyPlayers(UUID roomId) {
        readyPlayers.remove(roomId);

        log.info("Cleared ready players for room={}", roomId);
    }

    public void markPlayerBet(UUID roomId, UserInternalResponse user, BigDecimal bet) {
        PlayerBet newPlayerBet = playerBetFactory.create(user.guid(), bet, user.balance());
        playerBetValidator.validateBet(newPlayerBet);

        ClientSession newClient = getClientSessionByGuid(user.guid());

        Set<ClientSession> others = getPlayersInRoom(roomId).stream()
                .filter(player -> !player.getGuid().equals(newClient.getGuid()))
                .collect(Collectors.toSet());

        List<PlayerBet> bets = playerBets.computeIfAbsent(roomId, key -> new ArrayList<>());

        synchronized (bets) {
            bets.removeIf(playerBet -> playerBet.getGuid().equals(user.guid()));

            if (bets.isEmpty()) {
                bets.add(newPlayerBet);
                webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, others, DurakGameEvent.BET, bet);

                log.info("First bet in room={} by player={}: {}", roomId, user.username(), bet);

                return;
            }

            PlayerBet existingBet = bets.getFirst();
            ClientSession existingClient = getClientSessionByGuid(existingBet.getGuid());

            int compare = newPlayerBet.getBet().compareTo(existingBet.getBet());

            if (compare < 0) {
                webSocketHelper.notifyBetRejected(roomId, newClient, DurakGameEvent.BET_REJECT, newPlayerBet.getBet().toString());

                log.info("Bet rejected room={} player={}: {} (existing: {})", roomId, user.username(), bet, existingBet.getBet());

                return;
            }

            if (compare > 0) {
                bets.clear();
                bets.add(newPlayerBet);

                webSocketHelper.notifyBetAccepted(roomId, newClient, DurakGameEvent.BET, bet);
                webSocketHelper.notifyBetOutbid(roomId, existingClient, DurakGameEvent.BET_OUTBID, newPlayerBet.getBet());
                removeReadyPlayer(roomId, existingClient.getGuid());

                log.info("Bet outbid room={} by player={}: {} (outbid: {}, ready reset)", roomId, user.username(), bet, existingClient.getUsername());

                return;
            }

            bets.add(newPlayerBet);
            webSocketHelper.notifyBetAcceptedToAll(roomId, newClient, others, DurakGameEvent.BET, bet);

            log.info("Bet matched room={} player={}: {} — both ready to start", roomId, user.username(), bet);
        }
    }

    public List<PlayerBet> getPlayerBets(UUID roomId) {
        return new ArrayList<>(playerBets.getOrDefault(roomId, List.of()));
    }

    public void removePlayerBets(UUID roomId) {
        playerBets.remove(roomId);

        log.info("Cleared player bets for room={}", roomId);
    }

    public void validateBetsForGameStart(UUID roomId) {
        playerBetValidator.validateBetsForGameStart(getPlayerBets(roomId));
    }

    public UUID getOpponentId(UUID roomId, UUID playerId) {
        return getPlayersInRoom(roomId).stream()
                .map(ClientSession::getGuid)
                .filter(guid -> !guid.equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public List<UUID> getPlayerGuids(UUID roomId) {
        return getPlayersInRoom(roomId).stream()
                .map(ClientSession::getGuid)
                .collect(Collectors.toList());
    }

    private void removeReadyPlayer(UUID roomId, UUID playerGuid) {
        readyPlayers.computeIfPresent(roomId, (key, players) -> {
            boolean removed = players.remove(playerGuid);

            if (removed) {
                log.info("Removed ready status for player={} in room={} (bet outbid)", playerGuid, roomId);
            }

            return players.isEmpty() ? null : players;
        });
    }
}
