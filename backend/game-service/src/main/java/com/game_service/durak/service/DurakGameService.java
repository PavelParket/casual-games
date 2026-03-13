package com.game_service.durak.service;

import com.game_service.common.exception.NotFoundException;
import com.game_service.durak.domain.dto.DurakGameRequest;
import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.TablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakEvent;
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
import static com.game_service.durak.domain.enums.DurakEvent.ATTACKING;
import static com.game_service.durak.domain.enums.DurakEvent.DEFENDING;
import static com.game_service.durak.domain.enums.DurakEvent.PICKING_UP;
import static com.game_service.durak.domain.enums.DurakEvent.THROWING_MORE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DurakGameService {

    public static final long DEFAULT_TURN_TIME_MS = 30_000L;
    public static final long STALE_THRESHOLD_MS = 10 * 60 * 1_000L;

    private final ConcurrentHashMap<Long, Durak> activeGames = new ConcurrentHashMap<>();

    private final DurakGameValidator durakGameValidator;

    private final DurakGameMapper durakGameMapper;

    private final DurakRepository durakRepository;

    @Transactional
    public DurakGameResponse processStart(DurakGameRequest request) {
        Durak game = DurakGameUtils.initialize(request.roomId(), request.players());

        durakRepository.save(game);
        activeGames.putIfAbsent(game.getId(), game);

        log.info("Durak game started gameId={} roomId={} players={}", game.getId(), game.getRoomId(), game.getPlayers());

        return durakGameMapper.toStartResponse(game);
    }

    public DurakGameResponse processMove(DurakGameRequest request) {
        Durak game = activeGames.getOrDefault(request.id(), null);

        if (game == null) {
            throw new NotFoundException(DURAK_GAME_NOT_FOUND);
        }

        synchronized (game) {
            durakGameValidator.validate(game, request);

            applyMove(game, request);

            if (DurakEvent.GAME_OVER.equals(game.getEvent())) {
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

        log.info("Durak game finalized gameId={} status={}", game.getId(), game.getStatus());
    }

    private DurakGameResponse buildResponse(Durak game) {
        List<UUID> players = game.getPlayers();
        game.setLastActionAt(Instant.now());
        return durakGameMapper.toResponse(game, getAvailableActions(game, players.get(0)), getAvailableActions(game, players.get(1)));
    }

    public void applyMove(Durak game, DurakGameRequest request) {
        switch (game.getEvent()) {
            case ATTACKING -> applyAttacking(game, request.action(), request.card());
            case DEFENDING -> applyDefending(game, request.action(), request.card());
            case THROWING_MORE -> applyThrowingMore(game, request.action(), request.card());
            case PICKING_UP -> applyPickingUp(game, request.action(), request.card());
            default -> throw new IllegalStateException("Cannot apply non-playable phase: " + game.getEvent());
        }

        game.setLastActionAt(Instant.now());

        log.debug("Move applied gameId={} newPhase={} boutNumber={}", game.getId(), game.getEvent(), game.getBoutNumber());
    }

    private List<DurakAction> getAvailableActions(Durak game, UUID playerId) {
        if (!playerId.equals(game.getCurrentActorId())) {
            return List.of();
        }

        return switch (game.getEvent()) {
            case ATTACKING -> availableAttackingActions(game);
            case DEFENDING -> List.of(DurakAction.PLAY_CARD, DurakAction.TAKE_CARDS);
            case THROWING_MORE, PICKING_UP -> List.of(DurakAction.PLAY_CARD, DurakAction.PASS);
            default -> List.of();
        };
    }

    private void applyAttacking(Durak game, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(TablePair.attack(card));
                transition(game, DEFENDING, game.getDefenderId());
            }
            case PASS -> executeBoutEnd(game, false);
        }
    }

    private void applyDefending(Durak game, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getDefenderId(), card);

                List<TablePair> table = game.getTable();

                for (int i = 0; i < table.size(); i++) {
                    if (!table.get(i).isDefended()) {
                        table.set(i, table.get(i).withDefend(card));
                        break;
                    }
                }

                boolean allDefended = table.stream().allMatch(TablePair::isDefended);

                if (allDefended) {
                    transition(game, THROWING_MORE, game.getAttackerId());
                }
            }
            case TAKE_CARDS -> transition(game, PICKING_UP, game.getAttackerId());
        }
    }

    private void applyThrowingMore(Durak game, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(TablePair.attack(card));
                transition(game, DEFENDING, game.getDefenderId());
            }
            case PASS -> executeBoutEnd(game, false);
        }
    }

    private void applyPickingUp(Durak game, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(game, game.getAttackerId(), card);
                game.getTable().add(TablePair.attack(card));
            }
            case PASS -> {
                transferTableCardsToDefender(game);
                executeBoutEnd(game, true);
            }
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
        List<Card> defenderHand = game.defenderHand();

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
        game.setEvent(DurakEvent.GAME_OVER);

        log.info("Game over gameId={} winner={} boutNumber={}", game.getId(), game.getWinnerId(), game.getBoutNumber());
    }

    private void transition(Durak game, DurakEvent newPhase, UUID nextActor) {
        game.setEvent(newPhase);
        game.setCurrentActorId(nextActor);
    }

    private void removeFromHand(Durak game, UUID playerId, Card card) {
        List<Card> hand = game.getHands().get(playerId);
        boolean removed = hand.remove(card);

        if (!removed) {
            throw new IllegalStateException("Card " + card + " not found in hand of player " + playerId);
        }
    }

    private List<DurakAction> availableAttackingActions(Durak game) {
        List<DurakAction> actions = new ArrayList<>();
        actions.add(DurakAction.PLAY_CARD);

        boolean canPass = !game.getTable().isEmpty()
                && game.getTable().stream()
                .allMatch(TablePair::isDefended);

        if (canPass) {
            actions.add(DurakAction.PASS);

        }

        return actions;
    }

    private UUID resolveOpponent(Durak game, UUID playerId) {
        return game.getPlayers().stream()
                .filter(id -> !id.equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No opponent for " + playerId));
    }
}
