package com.websocket_hub.manager;

import com.websocket_hub.client.GameServiceClient;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalRequest;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.HorseRaceGamePreset;
import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import com.websocket_hub.domain.enums.redis.RoomPresetRedisKey;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomPresetRedisRepository;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.event.CountdownExpiredEvent;
import com.websocket_hub.factory.HorseRacePlayerBetFactory;
import com.websocket_hub.factory.RoomFactory;
import com.websocket_hub.helper.WebSocketHelper;
import com.websocket_hub.mapper.HorseRaceGameMessageMapper;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.serializer.MessageSerializer;
import com.websocket_hub.service.scheduler.HorseRaceRoomCountdownServiceScheduler;
import com.websocket_hub.validator.HorseRacePlayerBetValidator;
import com.websocket_hub.validator.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HorseRaceGameRoomManager extends AbstractRoomManager {

    private static final int COUNTDOWN_SECONDS = 90;

    private final Map<UUID, Set<UUID>> readyPlayers = new ConcurrentHashMap<>();

    private final Map<UUID, Map<UUID, HorseRacePlayerBet>> playerBets = new ConcurrentHashMap<>();

    private final Map<UUID, Long> countdownStartTimes = new ConcurrentHashMap<>();

    private final HorseRaceGameMessageMapper horseRaceGameMessageMapper;

    private final HorseRacePlayerBetValidator horseRacePlayerBetValidator;

    private final HorseRacePlayerBetFactory horseRacePlayerBetFactory;

    private final RoomPresetRedisRepository presetRedisRepository;

    private final GameServiceClient gameServiceClient;

    private final WebSocketHelper webSocketHelper;

    private final HorseRaceRoomCountdownServiceScheduler countdownServiceScheduler;

    private final ApplicationEventPublisher eventPublisher;

    public HorseRaceGameRoomManager(
            MessageSerializer serializer,
            RoomFactory roomFactory,
            SessionManager sessionManager,
            RoomValidator validator,
            RoomRedisRepository redisRepository,
            HorseRaceGameMessageMapper horseRaceGameMessageMapper,
            HorseRacePlayerBetValidator horseRacePlayerBetValidator,
            HorseRacePlayerBetFactory horseRacePlayerBetFactory,
            RoomPresetRedisRepository presetRedisRepository,
            GameServiceClient gameServiceClient,
            WebSocketHelper webSocketHelper,
            HorseRaceRoomCountdownServiceScheduler countdownServiceScheduler,
            ApplicationEventPublisher eventPublisher
    ) {
        super(serializer, roomFactory, sessionManager, validator, redisRepository);
        this.horseRaceGameMessageMapper = horseRaceGameMessageMapper;
        this.horseRacePlayerBetValidator = horseRacePlayerBetValidator;
        this.horseRacePlayerBetFactory = horseRacePlayerBetFactory;
        this.presetRedisRepository = presetRedisRepository;
        this.gameServiceClient = gameServiceClient;
        this.webSocketHelper = webSocketHelper;
        this.countdownServiceScheduler = countdownServiceScheduler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.HORSE_RACE;
    }

    @Override
    public MessageMapper getMapper() {
        return horseRaceGameMessageMapper;
    }

    @Override
    public RoomTypeRedisKey getRedisKey() {
        return RoomTypeRedisKey.HORSE_RACE_ROOM;
    }

    @Override
    protected void onAddSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} joined horse race room {}", user.email(), user.username(), room.getName());

        broadcast(room.getId(), horseRaceGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.JOIN,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has joined the room " + room.getName()
        ));

        sendCountdownToPlayer(room.getId(), user.guid());
    }

    @Override
    protected void onRemoveSession(UserInternalResponse user, Room room, WebSocketSession session) {
        log.info("Player email={} username={} left horse race room {}", user.email(), user.username(), room.getName());

        readyPlayers.computeIfPresent(room.getId(), (key, players) -> {
            players.remove(user.guid());
            return players.isEmpty() ? null : players;
        });

        playerBets.computeIfPresent(room.getId(), (key, bets) -> {
            bets.remove(user.guid());
            return bets.isEmpty() ? null : bets;
        });

        broadcast(room.getId(), horseRaceGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.LEAVE,
                user.guid(),
                null,
                room.getId(),
                "Player " + user.username() + " has left the room " + room.getName()
        ));
    }

    @Override
    protected void onCreateRoom(Room room) {
        try {
            HorseRaceGameInternalRequest createRequest = HorseRaceGameInternalRequest.builder()
                    .roomId(room.getId())
                    .build();

            HorseRaceGameInternalResponse createResponse = gameServiceClient.createRace(createRequest)
                    .orElseThrow(() -> new RuntimeException("Failed to create race preset from game-service"));

            HorseRaceGamePreset preset = horseRaceGameMessageMapper.toPreset(createResponse);

            presetRedisRepository.savePreset(room.getId(), preset, RoomPresetRedisKey.HORSE_RACE_PRESET);

            log.info("Race preset created and saved for room={}: horseCount={}, odds={}", room.getId(), preset.horseCount(), preset.odds());
        } catch (Exception e) {
            log.error("Failed to create preset for room={}: {}", room.getId(), e.getMessage());
            // TODO: decide on failure strategy - delete room or allow without preset?
        }

        countdownStartTimes.put(room.getId(), System.currentTimeMillis());
        countdownServiceScheduler.startCountdown(
                room.getId(),
                COUNTDOWN_SECONDS,
                () -> eventPublisher.publishEvent(new CountdownExpiredEvent(room.getId()))
        );

        log.info("Countdown started for room={}", room.getId());
    }

    @Override
    protected void onDeleteRoom(UUID roomId) {
        countdownServiceScheduler.cancelCountdown(roomId);
        countdownStartTimes.remove(roomId);

        presetRedisRepository.deletePreset(roomId, RoomPresetRedisKey.HORSE_RACE_PRESET);

        removeReadyPlayers(roomId);
        removePlayerBets(roomId);

        log.info("Horse race cleanup complete for room={}", roomId);
    }

    @Override
    public Integer getReadyPlayerCount(UUID roomId) {
        return readyPlayers.getOrDefault(roomId, Set.of()).size();
    }

    public void markReady(UUID roomId, UserInternalResponse user) {
        Room room = getRoomsMap().getOrDefault(roomId, null);

        if (room == null) {
            throw new IllegalArgumentException("Room id=" + roomId + " not found");
        }

        HorseRacePlayerBet playerBet = getPlayerBet(roomId, user.guid());

        if (playerBet == null) {
            log.warn("Player {} tried to ready without placing a bet in room={}", user.username(), roomId);
            ClientSession client = getClientSessionByGuid(user.guid());
            webSocketHelper.notifyBetRequired(roomId, client, HorseRaceEvent.BET_REQUIRED);
            return;
        }

        Set<UUID> ready = readyPlayers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        ready.add(user.guid());

        log.info("Player {} is ready in room {}. Total ready: {}", user.username(), roomId, ready.size());

        broadcast(roomId, horseRaceGameMessageMapper.toResponse(
                MessageType.SYSTEM,
                HorseRaceEvent.READY,
                user.guid(),
                null,
                roomId,
                "Player " + user.username() + " is ready"
        ));
    }

    public boolean areAllPlayersReady(UUID roomId) {
        Set<UUID> ready = readyPlayers.get(roomId);
        Set<ClientSession> players = getPlayersInRoom(roomId);

        return ready != null
                && !players.isEmpty()
                && ready.size() == players.size()
                && ready.containsAll(players.stream().map(ClientSession::getGuid).collect(Collectors.toSet()));
    }

    public Map<UUID, String> getParticipants(UUID roomId) {
        return getPlayersInRoom(roomId).stream()
                .collect(Collectors.toMap(
                        ClientSession::getGuid,
                        ClientSession::getUsername
                ));
    }

    public void removeReadyPlayers(UUID roomId) {
        readyPlayers.remove(roomId);
        log.info("Cleared ready players for room={}", roomId);
    }

    public HorseRaceGamePreset getPreset(UUID roomId) {
        return presetRedisRepository.getPreset(roomId, RoomPresetRedisKey.HORSE_RACE_PRESET, HorseRaceGamePreset.class);
    }

    public void removePreset(UUID roomId) {
        presetRedisRepository.deletePreset(roomId, RoomPresetRedisKey.HORSE_RACE_PRESET);
    }

    public void placeBet(UUID roomId, UserInternalResponse user, Integer horseIndex, BigDecimal amount) {
        ClientSession client = getClientSessionByGuid(user.guid());

        try {
            HorseRaceGamePreset preset = getPreset(roomId);

            if (preset == null) {
                throw new IllegalStateException("Preset not found for room=" + roomId);
            }

            Double odd = preset.odds().get(horseIndex);

            HorseRacePlayerBet playerBet = horseRacePlayerBetFactory.create(
                    user.guid(), horseIndex, odd, amount, user.balance()
            );

            horseRacePlayerBetValidator.validateBet(playerBet, preset.horseCount());

            playerBets.computeIfAbsent(roomId, key -> new ConcurrentHashMap<>())
                    .put(user.guid(), playerBet);

            log.info("Bet placed in room={} by player={}: horseIndex={}, odd={}, amount={}",
                    roomId, user.username(), horseIndex, odd, amount);

            webSocketHelper.notifyBetAccepted(roomId, client, HorseRaceEvent.BET, amount);
        } catch (Exception e) {
            log.warn("Bet rejected in room={} for player={}: {}", roomId, user.username(), e.getMessage());
            webSocketHelper.notifyBetRejected(roomId, client, HorseRaceEvent.BET_REJECT, e.getMessage());
        }
    }

    public HorseRacePlayerBet getPlayerBet(UUID roomId, UUID playerGuid) {
        Map<UUID, HorseRacePlayerBet> bets = playerBets.get(roomId);

        if (bets == null) {
            return null;
        }

        return bets.get(playerGuid);
    }

    public Collection<HorseRacePlayerBet> getPlayerBets(UUID roomId) {
        Map<UUID, HorseRacePlayerBet> bets = playerBets.get(roomId);

        if (bets == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(bets.values());
    }

    public boolean hasAnyBets(UUID roomId) {
        Map<UUID, HorseRacePlayerBet> bets = playerBets.get(roomId);
        return bets != null && !bets.isEmpty();
    }

    public void removePlayerBets(UUID roomId) {
        playerBets.remove(roomId);
        log.info("Cleared player bets for room={}", roomId);
    }

    public void cancelCountdown(UUID roomId) {
        countdownServiceScheduler.cancelCountdown(roomId);
        countdownStartTimes.remove(roomId);
    }

    private void sendCountdownToPlayer(UUID roomId, UUID playerGuid) {
        Long startTime = countdownStartTimes.get(roomId);

        if (startTime == null) {
            log.warn("No countdown start time found for room={}, skipping personal COUNTDOWN message", roomId);
            return;
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        int remaining = (int) Math.max(0, COUNTDOWN_SECONDS - elapsed);

        ClientSession client = getClientSessionByGuid(playerGuid);

        if (client == null) {
            log.warn("Client not found for player={}, skipping COUNTDOWN message", playerGuid);
            return;
        }

        sendToClient(client, horseRaceGameMessageMapper.toCountdownMessage(
                MessageType.SYSTEM,
                HorseRaceEvent.COUNTDOWN,
                null,
                playerGuid,
                roomId,
                remaining
        ));

        log.info("Sent COUNTDOWN message to player={} in room={}: remainingSeconds={}", playerGuid, roomId, remaining);
    }

    public void restartCountdown(UUID roomId) {
        countdownStartTimes.put(roomId, System.currentTimeMillis());

        countdownServiceScheduler.startCountdown(
                roomId,
                COUNTDOWN_SECONDS,
                () -> eventPublisher.publishEvent(new CountdownExpiredEvent(roomId))
        );

        broadcast(roomId, horseRaceGameMessageMapper.toCountdownMessage(
                MessageType.SYSTEM,
                HorseRaceEvent.COUNTDOWN,
                null,
                null,
                roomId,
                COUNTDOWN_SECONDS
        ));

        log.info("Countdown restarted for room={}", roomId);
    }
}
