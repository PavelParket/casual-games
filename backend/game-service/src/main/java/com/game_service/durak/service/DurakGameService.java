package com.game_service.durak.service;

import com.game_service.common.exception.NotFoundException;
import com.game_service.durak.domain.dto.DurakGameRequest;
import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.domain.dto.DurakPlayerViewResponse;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.entity.DurakTablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakPhase;
import com.game_service.durak.domain.enums.DurakStatus;
import com.game_service.durak.mapper.DurakGameMapper;
import com.game_service.durak.repository.DurakRepository;
import com.game_service.durak.utils.DurakGameUtils;
import com.game_service.durak.validator.DurakGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.game_service.config.ResourceMessageConstants.DURAK_GAME_NOT_FOUND;
import static com.game_service.durak.domain.enums.DurakPhase.ATTACKING;
import static com.game_service.durak.domain.enums.DurakPhase.DEFENDING;
import static com.game_service.durak.domain.enums.DurakPhase.PICKING_UP;
import static com.game_service.durak.domain.enums.DurakPhase.THROWING_MORE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DurakGameService {

    private final ConcurrentHashMap<Long, Durak> activeGames = new ConcurrentHashMap<>();

    private final DurakGameValidator durakGameValidator;

    private final DurakGameMapper durakGameMapper;

    private final DurakRepository durakRepository;

    @Transactional
    public DurakGameResponse processStart(DurakGameRequest request) {
        Durak game = DurakGameUtils.initialize(request.roomId(), request.players().getFirst(), request.players().getLast());

        durakRepository.save(game);
        activeGames.putIfAbsent(game.getId(), game);

        log.info("Durak game started gameId={} roomId={} players={}", game.getId(), game.getRoomId(), game.getPlayers());

        return buildResponse(game);
    }

    @Transactional
    public DurakGameResponse processMove(DurakGameRequest request) {
        Durak game = activeGames.getOrDefault(request.id(), null);

        if (game == null) {
            throw new NotFoundException(DURAK_GAME_NOT_FOUND);
        }

        synchronized (game) {
            if (!activeGames.containsKey(request.id())) {
                throw new NotFoundException(DURAK_GAME_NOT_FOUND);
            }

            durakGameValidator.validate(game, request);
            applyMove(game, request);

            if (DurakPhase.GAME_OVER.equals(game.getPhase())) {
                processResult(game);
            }

            return buildResponse(game);
        }
    }

    @Transactional
    public void processResult(Durak game) {
        game.setStatus(game.getWinnerId() != null ? DurakStatus.WINNER : DurakStatus.DRAW);
        durakRepository.save(game);
        activeGames.remove(game.getId());

        log.info("Durak game finalized gameId={} status={} winner={}", game.getId(), game.getStatus(), game.getWinnerId());
    }

    private DurakGameResponse buildResponse(Durak game) {
        if (DurakPhase.GAME_OVER.equals(game.getPhase())) {
            return durakGameMapper.toResponse(game, List.of());
        }

        List<UUID> players = game.getPlayers();
        UUID firstPlayerId = players.getFirst();
        UUID secondPlayerId = players.getLast();

        DurakPlayerViewResponse firstPlayerView = buildPlayerView(game, firstPlayerId, secondPlayerId);
        DurakPlayerViewResponse secondPlayerView = buildPlayerView(game, secondPlayerId, firstPlayerId);

        return durakGameMapper.toResponse(game, List.of(firstPlayerView, secondPlayerView));
    }

    private DurakPlayerViewResponse buildPlayerView(Durak game, UUID playerId, UUID opponentId) {
        List<DurakCard> myCards = List.copyOf(game.getHands().getOrDefault(playerId, List.of()));
        Integer opponentCardCount = game.getHands().getOrDefault(opponentId, List.of()).size();
        Boolean isMyTurn = playerId.equals(game.getCurrentActorId());
        List<DurakAction> availableActions = getAvailableActions(game, playerId);

        return durakGameMapper.toPlayerView(game, playerId, myCards, opponentCardCount, isMyTurn, availableActions);
    }

    public void applyMove(Durak game, DurakGameRequest request) {
        switch (game.getPhase()) {
            case ATTACKING -> applyAttacking(game, request.action(), request.card());

            case DEFENDING -> applyDefending(game, request.action(), request.card());

            case THROWING_MORE -> applyThrowingMore(game, request.action(), request.card());

            case PICKING_UP -> applyPickingUp(game, request.action(), request.card());

            default -> throw new IllegalStateException("Cannot apply non-playable phase: " + game.getPhase());
        }

        game.setLastActionAt(Instant.now());

        log.info("Move applied gameId={} newEvent={} boutNumber={}", game.getId(), game.getPhase(), game.getBoutNumber());
    }

    private List<DurakAction> getAvailableActions(Durak game, UUID playerId) {
        if (!playerId.equals(game.getCurrentActorId())) {
            return List.of();
        }

        return switch (game.getPhase()) {
            case ATTACKING -> availableAttackingActions(game);

            case DEFENDING -> List.of(DurakAction.PLAY_CARD, DurakAction.TAKE_CARDS);

            case THROWING_MORE -> game.defenderHand().isEmpty()
                    ? List.of(DurakAction.PASS)
                    : List.of(DurakAction.PLAY_CARD, DurakAction.PASS);

            case PICKING_UP -> List.of(DurakAction.PLAY_CARD, DurakAction.PASS);

            default -> List.of();
        };
    }

    private void applyAttacking(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(DurakTablePair.attack(card));
                transition(game, DEFENDING, game.getDefenderId());
            }

            case PASS -> executeBoutEnd(game, false);

            default -> throw new IllegalStateException("Unexpected action in ATTACKING: " + action);
        }
    }

    private void applyDefending(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getDefenderId(), card);

                List<DurakTablePair> table = game.getTable();

                for (int i = 0; i < table.size(); i++) {
                    if (!table.get(i).isDefended()) {
                        table.set(i, table.get(i).withDefend(card));
                        break;
                    }
                }

                boolean allDefended = table.stream().allMatch(DurakTablePair::isDefended);

                if (allDefended) {
                    if (game.attackerHand().isEmpty() || DurakGameUtils.isGameOver(game)) {
                        executeBoutEnd(game, false);
                    } else {
                        transition(game, THROWING_MORE, game.getAttackerId());
                    }
                }
            }

            case TAKE_CARDS -> transition(game, PICKING_UP, game.getAttackerId());

            default -> throw new IllegalStateException("Unexpected action in DEFENDING: " + action);
        }
    }

    private void applyThrowingMore(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(DurakTablePair.attack(card));
                transition(game, DEFENDING, game.getDefenderId());
            }

            case PASS -> executeBoutEnd(game, false);

            default -> throw new IllegalStateException("Unexpected action in THROWING_MORE: " + action);
        }
    }

    private void applyPickingUp(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(DurakTablePair.attack(card));
            }

            case PASS -> {
                transferTableCardsToDefender(game);
                executeBoutEnd(game, true);
            }

            default -> throw new IllegalStateException("Unexpected action in PICKING_UP: " + action);
        }
    }

    private void executeBoutEnd(Durak game, boolean defenderTookCards) {
        game.getTable().clear();
        DurakGameUtils.dealCards(game);

        if (DurakGameUtils.isGameOver(game)) {
            finalizeGameOver(game);
            return;
        }

        game.setBoutNumber(game.getBoutNumber() + 1);

        if (defenderTookCards) {
            transition(game, ATTACKING, game.getAttackerId());
        } else {
            swapRolesAndAttack(game);
        }
    }

    private void transferTableCardsToDefender(Durak game) {
        List<DurakCard> defenderHand = game.defenderHand();

        game.getTable().forEach(pair -> {
            defenderHand.add(pair.attackCard());

            if (pair.defendCard() != null) {
                defenderHand.add(pair.defendCard());
            }
        });
    }

    private void swapRolesAndAttack(Durak game) {
        UUID newAttacker = game.getDefenderId();
        UUID newDefender = game.getAttackerId();
        game.setAttackerId(newAttacker);
        game.setDefenderId(newDefender);
        transition(game, ATTACKING, newAttacker);
    }

    private void finalizeGameOver(Durak game) {
        Optional<UUID> winner = DurakGameUtils.determineWinner(game);
        game.setWinnerId(winner.orElse(null));
        game.setPhase(DurakPhase.GAME_OVER);

        log.info("Game over gameId={} winner={} boutNumber={}", game.getId(), game.getWinnerId(), game.getBoutNumber());
    }

    private void transition(Durak game, DurakPhase newPhase, UUID nextActor) {
        game.setPhase(newPhase);
        game.setCurrentActorId(nextActor);
    }

    private void removeFromHand(Durak game, UUID playerId, DurakCard card) {
        List<DurakCard> hand = game.getHands().get(playerId);
        boolean removed = hand.remove(card);

        if (!removed) {
            throw new IllegalStateException("Card " + card + " not found in hand of player " + playerId);
        }
    }

    private List<DurakAction> availableAttackingActions(Durak game) {
        List<DurakAction> actions = new ArrayList<>();
        actions.add(DurakAction.PLAY_CARD);

        boolean canPass = !game.getTable().isEmpty() && game.getTable().stream().allMatch(DurakTablePair::isDefended);

        if (canPass) {
            actions.add(DurakAction.PASS);

        }

        return actions;
    }

    @Transactional
    public DurakGameResponse processTimeout(DurakGameRequest request) {
        Durak game = activeGames.getOrDefault(request.id(), null);

        if (game == null) {
            log.warn("Timeout received for unknown or already finished game: gameId={}", request.id());

            throw new NotFoundException(DURAK_GAME_NOT_FOUND);
        }

        synchronized (game) {
            if (!activeGames.containsKey(request.id())) {
                throw new NotFoundException(DURAK_GAME_NOT_FOUND);
            }

            game.setWinnerId(request.winnerId());
            game.setPhase(DurakPhase.GAME_OVER);
            processResult(game);
        }

        log.info("Durak game finalized by timeout: gameId={} winner={}", request.id(), request.winnerId());

        return buildResponse(game);
    }
}
