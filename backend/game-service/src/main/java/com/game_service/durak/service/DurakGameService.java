package com.game_service.durak.service;

import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.TablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakEvent;
import com.game_service.durak.utils.DurakGameUtils;
import com.game_service.durak.validator.DurakGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final DurakMapper durakMapper;

    private final DurakRepository durakRepository;

    public DurakGameResponse processStart()

    public void applyMove(Durak match, UUID playerId, DurakAction action, Card card) {
        switch (match.getPhase()) {
            case ATTACKING -> applyAttacking(match, action, card);
            case DEFENDING -> applyDefending(match, action, card);
            case THROWING_MORE -> applyThrowingMore(match, action, card);
            case PICKING_UP -> applyPickingUp(match, action, card);
            default -> throw new IllegalStateException("Cannot apply non-playable phase: " + match.getPhase());
        }

        match.setLastActionAt(Instant.now());

        log.debug("Move applied gameId={} newPhase={} boutNumber={}", match.getId(), match.getPhase(), match.getBoutNumber());
    }

    public List<DurakAction> getAvailableActions(Durak match, UUID playerId) {
        if (!playerId.equals(match.getCurrentActorId())) {
            return List.of();
        }

        return switch (match.getPhase()) {
            case ATTACKING -> availableAttackingActions(match);
            case DEFENDING -> List.of(DurakAction.PLAY_CARD, DurakAction.TAKE_CARDS);
            case THROWING_MORE, PICKING_UP -> List.of(DurakAction.PLAY_CARD, DurakAction.PASS);
            default -> List.of();
        };
    }

    private void applyAttacking(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
                transition(match, DEFENDING, match.getDefenderId());
            }
            case PASS -> executeBoutEnd(match, false);
        }
    }

    private void applyDefending(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getDefenderId(), card);

                List<TablePair> table = match.getTable();

                for (int i = 0; i < table.size(); i++) {
                    if (!table.get(i).isDefended()) {
                        table.set(i, table.get(i).withDefend(card));
                        break;
                    }
                }

                boolean allDefended = table.stream().allMatch(TablePair::isDefended);

                if (allDefended) {
                    transition(match, THROWING_MORE, match.getAttackerId());
                }
            }
            case TAKE_CARDS -> transition(match, PICKING_UP, match.getAttackerId());
        }
    }

    private void applyThrowingMore(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
                transition(match, DEFENDING, match.getDefenderId());
            }
            case PASS -> executeBoutEnd(match, false);
        }
    }

    private void applyPickingUp(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
            }
            case PASS -> {
                transferTableCardsToDefender(match);
                executeBoutEnd(match, true);
            }
        }
    }

    private void executeBoutEnd(Durak match, boolean defenderTookCards) {
        match.getTable().clear();
        DurakGameUtils.dealCards(match);

        if (DurakGameUtils.isGameOver(match)) {
            finalizeGameOver(match);
            return;
        }

        match.setBoutNumber(match.getBoutNumber() + 1);

        if (defenderTookCards) {
            transition(match, ATTACKING, match.getAttackerId());
        } else {
            swapRolesAndAttack(match);
        }
    }

    private void transferTableCardsToDefender(Durak match) {
        List<Card> defenderHand = match.defenderHand();

        for (TablePair pair : match.getTable()) {
            defenderHand.add(pair.attackCard());

            if (pair.defendCard() != null) {
                defenderHand.add(pair.defendCard());
            }
        }
    }

    private void swapRolesAndAttack(Durak match) {
        UUID newAttacker = match.getDefenderId();
        UUID newDefender = match.getAttackerId();
        match.setAttackerId(newAttacker);
        match.setDefenderId(newDefender);
        transition(match, ATTACKING, newAttacker);
    }

    private void finalizeGameOver(Durak match) {
        Optional<UUID> winner = DurakGameUtils.determineWinner(match);
        match.setWinnerId(winner.orElse(null));
        match.setPhase(DurakEvent.GAME_OVER);

        log.info("Game over gameId={} winner={} boutNumber={}", match.getId(), match.getWinnerId(), match.getBoutNumber());
    }

    private void transition(Durak match, DurakEvent newPhase, UUID nextActor) {
        match.setPhase(newPhase);
        match.setCurrentActorId(nextActor);
    }

    private void removeFromHand(Durak match, UUID playerId, Card card) {
        List<Card> hand = match.getHands().get(playerId);
        boolean removed = hand.remove(card);

        if (!removed) {
            throw new IllegalStateException("Card " + card + " not found in hand of player " + playerId);
        }
    }

    private List<DurakAction> availableAttackingActions(Durak match) {
        List<DurakAction> actions = new ArrayList<>();
        actions.add(DurakAction.PLAY_CARD);

        boolean canPass = !match.getTable().isEmpty() && match.getTable().stream().allMatch(TablePair::isDefended);

        if (canPass) {
            actions.add(DurakAction.PASS);

        }
        return actions;
    }
}
